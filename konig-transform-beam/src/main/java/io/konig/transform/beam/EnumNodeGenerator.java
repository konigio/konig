package io.konig.transform.beam;

import java.text.MessageFormat;

/*
 * #%L
 * Konig Transform Beam
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.openrdf.model.URI;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.IJStatement;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlEnumJoinInfo;
import io.konig.core.showl.ShowlEnumNodeExpression;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlStatement;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;

public class EnumNodeGenerator extends TargetPropertyGenerator {

	public EnumNodeGenerator(BeamExpressionTransform etran) {
		super(etran);
	}

	protected RdfJavaType returnType(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		URI rdfType = targetProperty.getValueType(etran.getOwlReasoner());
		AbstractJClass tableRowClass = etran.codeModel().ref(TableRow.class);
		
		return new RdfJavaType(rdfType, tableRowClass);
	}

	@Override
	protected void generateBody(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		
		JVar enumRow = declareEnumMember(beamMethod, targetProperty);
		StructInfo structInfo = processNode(beamMethod, targetProperty.getValueShape());

		for (BeamMethod propertyMethod : structInfo.getMethodList()) {
			etran.invoke(propertyMethod);
		}
	
		
		captureValue(targetProperty, enumRow.invoke("isEmpty").not(), enumRow);
		beamMethod.getMethod().body()._return(enumRow);
		
	}


	private StructInfo processNode(BeamMethod beamMethod, ShowlNodeShape node) throws BeamTransformGenerationException {
		
		StructPropertyGenerator generator  = new StructPropertyGenerator(etran);
		
		return generator.processNode(beamMethod, node);
	}

	@Override
	protected void addParameters(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		
		etran.addOutputRowAndErrorBuilderParams(beamMethod, targetProperty);
		
//		ShowlStatement joinStatement = joinStatement(targetProperty);
//		
//		Set<ShowlPropertyShape> set = new HashSet<>();
//		joinStatement.addProperties(set);
//		etran.addParametersFromPropertySet(beamMethod, targetProperty, set);
//		etran.addOutputRowAndErrorBuilderParams(beamMethod, targetProperty);
	}

	private JVar declareEnumMember(BeamMethod beamMethod, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		
		ShowlEnumJoinInfo joinInfo = ShowlEnumJoinInfo.forEnumProperty(targetProperty);
		if (joinInfo == null) {
			fail("Failed to find ShowlEnumJoinInfo for {0}", targetProperty.getPath());
		}
		
		RdfJavaType type = etran.getTypeManager().rdfJavaType(targetProperty);
		
		AbstractJClass enumType = (AbstractJClass) type.getJavaType();

		BlockInfo blockInfo = etran.peekBlockInfo();
		
		
		JBlock block = blockInfo.getBlock();

		JVar rowVar= null;
		ShowlEffectiveNodeShape enumNode = targetProperty.getValueShape().effectiveNode();
		ShowlNodeShape enumClassNode = ShowlUtil.enumClassNode(targetProperty.getValueShape());

		AbstractJClass tableRowClass = etran.codeModel().ref(TableRow.class);
		
		String tableRowName = targetProperty.getPredicate().getLocalName() + "Row";
		
		rowVar = block.decl(tableRowClass, tableRowName).init(tableRowClass._new());
		blockInfo.putTableRow(enumNode, rowVar);
		blockInfo.putTableRow(joinInfo.getEnumProperty().getDeclaringShape().effectiveNode(), rowVar);

		JVar errorBuilder = blockInfo.getErrorBuilderVar();
		IJExpression fieldValue = null;

		IJStatement errorStatement = null;
		String sourcePropertyPath = null;
		JVar sourcePropertyVar = null;
		
		if (joinInfo.getSourceProperty() != null && joinInfo.getTargetProperty()!=null) {
			ShowlPropertyShape sourceProperty = joinInfo.getSourceProperty();
			ShowlPropertyExpression e = ShowlPropertyExpression.of(sourceProperty);
			IJExpression sourceField = etran.transform(e);
			
			sourcePropertyPath = sourceProperty.getPath();
			
			RdfJavaType sourceFieldType = etran.getTypeManager().rdfJavaType(sourceProperty);
			
			
			URI enumPropertyId = joinInfo.getEnumProperty().getPredicate();
			
		
			sourcePropertyVar = etran.declarePropertyValue(sourceProperty, sourceField, sourceFieldType.getJavaType());
			
			JConditional ifStatement = block._if(sourcePropertyVar.eqNull());
			if (targetProperty.isRequired()) {
				String msg = MessageFormat.format("Cannot set required property ''{0}'' because ''{1}'' is null", 
						targetProperty.fullPath(), sourceProperty.getPath());
				
				ifStatement._then().add(errorBuilder.invoke("addError").arg(JExpr.lit(msg)));
				
			}
			ifStatement._then()._return(rowVar);
			
			
			String findMethodName = Konig.id.equals(enumPropertyId) ?
					"findByLocalName" :
					"findBy" + StringUtil.capitalize(enumPropertyId.getLocalName());
			
			fieldValue = enumType.staticInvoke(findMethodName).arg(sourcePropertyVar);
			

			
			
		} else if (joinInfo.getExpression() != null) {
			fieldValue = etran.transform(joinInfo.getExpression());
			
			if (targetProperty.isRequired()) {
			
				String errorText = MessageFormat.format("Cannot set {0} because {1} evaluates to null", 
						targetProperty, 
						joinInfo.getExpression().displayValue());

				errorStatement = errorBuilder.invoke("addError").arg(JExpr.lit(errorText));
			}
			
		} else if (joinInfo.getHardCodedReference()!=null) {
			
			URI memberId = joinInfo.getHardCodedReference().getIriValue();
			fieldValue = enumType.staticInvoke("findByLocalName").arg(JExpr.lit(memberId.getLocalName()));
			// TODO: verify that memberId is, in fact, a valid IRI for a member of the given enumeration
			
		} else {
			fail("Failed to add enum parameter for {0}", targetProperty.getPath());
		}

		String fieldName = blockInfo.varName(targetProperty.getPredicate().getLocalName());
		JVar enumVar = block.decl(enumType, fieldName).init(fieldValue);
		
		// TODO: consider a redesign so that we don't have to map both enumNode and enumClassNode to enumVar
		
		blockInfo.putEnumMember(enumNode, enumVar);
		blockInfo.putEnumMember(enumClassNode.effectiveNode(), enumVar);

		JConditional ifStatement = block._if(enumVar.eqNull());
		
		if (sourcePropertyPath != null) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("Cannot set ");
			errorMessage.append(targetProperty.fullPath());
			errorMessage.append(" because ");
			errorMessage.append(sourcePropertyPath);
			errorMessage.append(" = ''{0}'' does not map to a valid enum value");
	
			AbstractJClass messageFormatClass = etran.codeModel().ref(MessageFormat.class);
			AbstractJClass stringClass = etran.codeModel().ref(String.class);
			JVar messageVar = ifStatement._then().decl(stringClass, blockInfo.varName("msg")).init(messageFormatClass.staticInvoke("format")
					.arg(JExpr.lit(errorMessage.toString())).arg(sourcePropertyVar));
			errorStatement = errorBuilder.invoke("addError").arg(messageVar);
		}
		
		if (errorStatement != null) {
			ifStatement._then().add(errorStatement);
		}
		ifStatement._then()._return(rowVar);
		
		return rowVar;
	}

	private ShowlStatement joinStatement(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		ShowlExpression e = targetProperty.getSelectedExpression();
		if (e instanceof ShowlEnumNodeExpression) {
			ShowlEnumNodeExpression enumNodeExpr = (ShowlEnumNodeExpression) e;
			ShowlChannel channel = enumNodeExpr.getChannel();
			if (channel != null && channel.getJoinStatement()!=null) {
				return channel.getJoinStatement();
			}
			
		}
		throw new BeamTransformGenerationException("Join statement not found for " + targetProperty.getPath());
	}


}

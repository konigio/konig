package io.konig.transform.beam;

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


import java.text.MessageFormat;
import java.util.Map.Entry;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlAlternativePathsExpression;
import io.konig.core.showl.ShowlBasicStructExpression;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlEnumIndividualReference;
import io.konig.core.showl.ShowlEnumPropertyExpression;
import io.konig.core.showl.ShowlEnumStructExpression;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.util.StringUtil;

public class AlternativePathsGenerator extends StructPropertyGenerator {

	public AlternativePathsGenerator(BeamExpressionTransform etran) {
		super(etran);
	}
	
	protected RdfJavaType returnType(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		return new RdfJavaType(XMLSchema.BOOLEAN, etran.codeModel()._ref(boolean.class));
	}

	@Override
	protected void generateBody(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		

		ShowlAlternativePathsExpression e = (ShowlAlternativePathsExpression) targetProperty.getSelectedExpression();
		
		generateAlternativePaths(beamMethod, targetProperty, e);

	}

	private void generateAlternativePaths(BeamMethod beamMethod, ShowlPropertyShape targetProperty, ShowlAlternativePathsExpression e) throws BeamTransformGenerationException {
		
		JCodeModel codeModel = etran.codeModel();
		
		AbstractJType booleanType = codeModel._ref(boolean.class);
		
		JBlock block = beamMethod.getMethod().body();
		
		JVar ok = block.decl(booleanType, "ok").init(JExpr.FALSE);
		
		int memberIndex = 0;
		for (ShowlExpression member : e.getMemberList()) {
			
			StringBuilder methodName = new StringBuilder();
			methodName.append(beamMethod.name());
			methodName.append('_');
			methodName.append(memberIndex++);
			
			JMethod method = etran.getTargetClass().method(JMod.PRIVATE, booleanType, methodName.toString());
			
			BeamMethod beamMemberMethod = new BeamMethod(method);
			buildMemberMethod(beamMemberMethod, member, targetProperty);
			
			JInvocation invoke = etran.createInvocation(beamMemberMethod);
			
			block.assign(ok, ok.cor(invoke));
		}
		
		if (targetProperty.isRequired()) {
			JVar errorBuilder = etran.peekBlockInfo().getErrorBuilderVar();
			String msg = MessageFormat.format("No value found for required property ''{0}'' ", 
					targetProperty.getPredicate().getLocalName());

			JConditional ifStatement = block._if(ok.not());
			ifStatement._then().add(errorBuilder.invoke("addError").arg(JExpr.lit(msg)));
		}

		block._return(ok);
		
	}

	private void buildMemberMethod(BeamMethod beamMemberMethod, ShowlExpression member, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		
		if (member instanceof ShowlBasicStructExpression) {
			basicStruct(beamMemberMethod, (ShowlBasicStructExpression) member, targetProperty);
		} else {
			fail("Expression type {0} not supported for {1}", member.getClass().getSimpleName(), targetProperty.getPath());
		}
	}
	
	private void enumStruct(BeamMethod beamMethod, ShowlEnumStructExpression struct, ShowlPropertyShape targetProperty)
	throws BeamTransformGenerationException {
		BlockInfo blockInfo = etran.beginBlock(beamMethod);
		try {
			ShowlNodeShape enumNode = struct.getEnumNode();
			JVar outputRow = etran.addTableRowParam(beamMethod, targetProperty.getDeclaringShape().effectiveNode());
			declareEnumMemberVar(struct, targetProperty);
		
			beamMethod.excludeParamFor(BeamParameter.pattern(BeamParameterType.TABLE_ROW, targetProperty.getValueShape().effectiveNode()));
			beamMethod.excludeParamFor(BeamParameter.pattern(BeamParameterType.TABLE_ROW, enumNode.effectiveNode()));
			
			
			JCodeModel model = etran.codeModel();
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			JBlock block = beamMethod.getMethod().body();
			
			String enumRowName = blockInfo.varName(targetProperty.getPredicate().getLocalName() + "Row");
			JVar enumRow = block.decl(tableRowClass, enumRowName).init(tableRowClass._new());
			blockInfo.putTableRow(targetProperty.getValueShape().effectiveNode(), enumRow);
			blockInfo.putTableRow(enumNode.effectiveNode(), enumRow);
			
			
			JCodeModel codeModel = etran.codeModel();
			AbstractJType booleanType = codeModel._ref(boolean.class);
			
			ShowlNodeShape node = targetProperty.getValueShape();

			JVar ok = block.decl(booleanType, "ok");
			if (struct.size()==1) {
				Entry<URI, ShowlExpression> entry = struct.entrySet().iterator().next();

				URI predicate = entry.getKey();
				ShowlExpression e = entry.getValue();
				
				ShowlPropertyShape p = node.getProperty(predicate);
				
				
				BeamMethod fieldMethod = fieldMethod(beamMethod, p, e);
				
				JInvocation invoke = etran.createInvocation(fieldMethod);
				ok.init(invoke);
				
				
			} else {
			
				ok.init(JExpr.TRUE);
				
				for (Entry<URI, ShowlExpression> entry : struct.entrySet()) {
					URI predicate = entry.getKey();
					ShowlExpression e = entry.getValue();
					
					ShowlPropertyShape p = node.getProperty(predicate);
					
					
					BeamMethod fieldMethod = fieldMethod(beamMethod, p, e);
					
					JInvocation invoke = etran.createInvocation(fieldMethod);
					
					block.assign(ok, ok.cand(invoke));
				}
				
			}

			JConditional ifStatement = block._if(ok);
			ifStatement._then().add(outputRow.invoke("put")
					.arg(JExpr.lit(targetProperty.getPredicate().getLocalName())).arg(enumRow));
			block._return(ok);
			
		} finally {
			etran.endBlock();
		}
	}


	private JVar declareEnumMemberVar(ShowlEnumStructExpression struct, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		
		
		ShowlNodeShape enumNode = struct.getEnumNode();
		
		ShowlChannel channel = targetProperty.getRootNode().findChannelFor(enumNode);
		if (channel == null) {
			fail("Channel for {0} not found while processing {1}", enumNode.getPath(), targetProperty.getPath());
		}
		
		return etran.declareEnumIndividual(enumNode, channel.getJoinStatement());
	}

	private void basicStruct(BeamMethod beamMethod, ShowlBasicStructExpression struct, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {

		BlockInfo blockInfo = etran.beginBlock(beamMethod);
		try {
			JVar outputRow = etran.addTableRowParam(beamMethod, targetProperty.getDeclaringShape().effectiveNode());
		
			beamMethod.excludeParamFor(BeamParameter.pattern(BeamParameterType.TABLE_ROW, targetProperty.getValueShape().effectiveNode()));
			
			
			JCodeModel model = etran.codeModel();
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			JBlock block = beamMethod.getMethod().body();
	
			JVar structVar = block.decl(tableRowClass, targetProperty.getPredicate().getLocalName()).init(tableRowClass._new());
			blockInfo.putTableRow(targetProperty.getValueShape().effectiveNode(), structVar);
			
			JCodeModel codeModel = etran.codeModel();
			AbstractJType booleanType = codeModel._ref(boolean.class);
			
			ShowlNodeShape node = targetProperty.getValueShape();

			JVar ok = block.decl(booleanType, "ok");
			if (struct.size()==1) {
				Entry<URI, ShowlExpression> entry = struct.entrySet().iterator().next();

				URI predicate = entry.getKey();
				ShowlExpression e = entry.getValue();
				
				ShowlPropertyShape p = node.getProperty(predicate);
				
				
				BeamMethod fieldMethod = fieldMethod(beamMethod, p, e);
				
				JInvocation invoke = etran.createInvocation(fieldMethod);
				ok.init(invoke);
				
				
			} else {
			
				ok.init(JExpr.TRUE);
				
				for (Entry<URI, ShowlExpression> entry : struct.entrySet()) {
					URI predicate = entry.getKey();
					ShowlExpression e = entry.getValue();
					
					ShowlPropertyShape p = node.getProperty(predicate);
					
					
					BeamMethod fieldMethod = fieldMethod(beamMethod, p, e);
					
					JInvocation invoke = etran.createInvocation(fieldMethod);
					
					block.assign(ok, ok.cand(invoke));
				}
				
			}

			JConditional ifStatement = block._if(ok);
			ifStatement._then().add(outputRow.invoke("put")
					.arg(JExpr.lit(targetProperty.getPredicate().getLocalName())).arg(structVar));
			block._return(ok);
			
		} finally {
			etran.endBlock();
		}
		
	}

	protected BeamMethod fieldMethod(BeamMethod callerMethod, ShowlPropertyShape targetProperty, ShowlExpression e) throws BeamTransformGenerationException {
		JCodeModel model = etran.codeModel();
		AbstractJType booleanType = model._ref(boolean.class);
		
		URI predicate = targetProperty.getPredicate();
		String methodName = callerMethod.name() + "_" + predicate.getLocalName();
		
		JMethod method = etran.getTargetClass().method(JMod.PRIVATE, booleanType, methodName);
		BeamMethod beamMethod = new BeamMethod(method);
		
		if (e instanceof ShowlBasicStructExpression) {
			basicStruct(beamMethod, (ShowlBasicStructExpression) e, targetProperty);
		} else if (e instanceof ShowlEnumPropertyExpression) {
			enumProperty(beamMethod, (ShowlEnumPropertyExpression)e, targetProperty);
		} else if (e instanceof ShowlEnumStructExpression) {
			enumStruct(beamMethod, (ShowlEnumStructExpression)e, targetProperty);
		} else if (e instanceof ShowlEnumIndividualReference) {
			enumIndividualReference(beamMethod, (ShowlEnumIndividualReference)e, targetProperty);
		} else {
			simpleField(beamMethod, e, targetProperty);
		}
		
		
		return beamMethod;
	}

	private void enumProperty(BeamMethod beamMethod, ShowlEnumPropertyExpression e, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {

		BlockInfo blockInfo = etran.beginBlock(beamMethod);
		try {
			AbstractJClass objectClass = etran.codeModel().ref(Object.class);
			
			ShowlPropertyShape enumAccessor = targetProperty.getDeclaringShape().getAccessor();
			if (enumAccessor == null) {
				fail("enum accessor not found while processing {0}", targetProperty.getPath());
			}
			
			JVar enumMemberVar = etran.addEnumParamForEnumProperty(beamMethod, targetProperty);
			

			JVar outputRow = etran.addTableRowParam(beamMethod, targetProperty.getDeclaringShape().effectiveNode());
			
			String propertyName = e.getSourceProperty().getPredicate().getLocalName();
			String getter = "get" + StringUtil.capitalize(propertyName);
			
			IJExpression fieldValue = enumMemberVar.invoke(getter);
			
			JBlock block = beamMethod.getMethod().body();
			String fieldName = targetProperty.getPredicate().getLocalName();
			
			JVar fieldVar = block.decl(objectClass, blockInfo.varName(fieldName)).init(fieldValue);
			
			JConditional ifStatement = block._if(fieldVar.neNull());
			ifStatement._then().add(outputRow.invoke("put").arg(JExpr.lit(fieldName)).arg(fieldVar));
			ifStatement._then()._return(JExpr.TRUE);
			
			block._return(JExpr.FALSE);
		} finally {
			etran.endBlock();
		}
		
	}

	private void enumIndividualReference(BeamMethod beamMethod, ShowlEnumIndividualReference e, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		BlockInfo blockInfo = etran.beginBlock(beamMethod);
		try {
			JBlock block = blockInfo.getBlock();
			JVar outputRow = etran.addTableRowParam(beamMethod, targetProperty.getDeclaringShape().effectiveNode());
			
			String fieldName = targetProperty.getPredicate().getLocalName();
			IJExpression fieldValue = JExpr.lit(e.getIriValue().getLocalName());
			block.add(outputRow.invoke("put").arg(JExpr.lit(fieldName)).arg(fieldValue));
			block._return(JExpr.TRUE);
			
		} finally {
			etran.endBlock();
		}
		
	}

	private void simpleField(BeamMethod beamMethod, ShowlExpression e, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {

		etran.beginBlock(beamMethod);
		try {
			AbstractJClass objectClass = etran.codeModel().ref(Object.class);
			JVar outputRow = etran.addTableRowParam(beamMethod, targetProperty.getDeclaringShape().effectiveNode());
			
			IJExpression fieldValue = etran.transform(e);
			
			JBlock block = beamMethod.getMethod().body();
			String fieldName = targetProperty.getPredicate().getLocalName();
			
			JVar fieldVar = block.decl(objectClass, fieldName).init(fieldValue);
			
			JConditional ifStatement = block._if(fieldVar.neNull());
			ifStatement._then().add(outputRow.invoke("put").arg(JExpr.lit(fieldName)).arg(fieldVar));
			ifStatement._then()._return(JExpr.TRUE);
			
			block._return(JExpr.FALSE);
		} finally {
			etran.endBlock();
		}
	}

}

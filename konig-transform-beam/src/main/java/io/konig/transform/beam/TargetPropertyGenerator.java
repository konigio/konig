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

import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlAlternativePathsExpression;
import io.konig.core.showl.ShowlArrayExpression;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlUtil;

public abstract class TargetPropertyGenerator {

	protected BeamExpressionTransform etran;
	
	public static TargetPropertyGenerator create(BeamExpressionTransform etran, ShowlDirectPropertyShape p) throws BeamTransformGenerationException {
		
		
		if (p.getValueShape()!=null && p.getValueShape().getOwlClass().isEnum(etran.getOwlReasoner())) {
			return new EnumNodeGenerator(etran);
		}
		
		ShowlExpression e = p.getSelectedExpression();
		if (ShowlUtil.isEnumField(e)) {
			return new EnumPropertyGenerator(etran);
		}

		if (e instanceof ShowlArrayExpression) {
			return new MultiValuedTargetPropertyGenerator(etran);
		}
		
		RdfJavaType type = etran.getTypeManager().rdfJavaType(p);
		
		if (type.isSimpleType()) {
			return new SimplePropertyGenerator(etran);
		}
		
		if (p.getValueShape()!=null) {
			
			if (p.getSelectedExpression() instanceof ShowlAlternativePathsExpression) {
				return new AlternativePathsGenerator(etran);
			}
			
			return new StructPropertyGenerator(etran);
		}
		
		throw new BeamTransformGenerationException("Type not supported yet: " + type.getRdfType().getLocalName());
	}
	

	public TargetPropertyGenerator(BeamExpressionTransform etran) {
		this.etran = etran;
	}
	
	/**
	 * Set the value of a target property in the output row, but only if the value is well-defined.
	 * The value is well-defined if it is not null and, in the case of a TableRow or List, not empty.
	 * If the target property is required and the value is not well-defined, add an error message to the ErrorBuilder.
	 */
	protected void captureValue(ShowlPropertyShape targetProperty, JVar var) throws BeamTransformGenerationException {

		BlockInfo blockInfo = etran.peekBlockInfo();
		
		boolean requiresNullCheck = true;
		IJExpression varInit = var.init();
		if (varInit instanceof JInvocation) {
			JInvocation invoke = (JInvocation) varInit;
			if (invoke.isConstructor()) {
				// Since var was initialized by a constructor, we know that it is not null.
				// Hence, we do not need to perform a null check.
				requiresNullCheck = false;
			}
		}
		
		IJExpression wellDefined = requiresNullCheck ? var.neNull() : null;
		
		// If the value is a TableRow or a List, we need to confirm that it is not empty.
		// There are probably some cases where this check is not necessary since it is provably not empty.
		// But it certainly won't hurt to always perform the check.
		
		String varName = var.type().name();
		
		if (varName.equals("TableRow") || varName.startsWith("List") || varName.endsWith("List")) {
			IJExpression notEmpty = var.invoke("isEmpty").not();
			wellDefined = (wellDefined==null) ? notEmpty : wellDefined.cand(notEmpty);
		}
		
				
		// If wellDefined is null, then it must be the case that the value is provably well-defined and we
		// don't have to use an if statement to check whether it is well-defined.
		
		JConditional ifStatement = wellDefined!=null ? blockInfo.getBlock()._if(wellDefined) : null;
		
		ShowlEffectiveNodeShape node = targetProperty.getDeclaringShape().effectiveNode();
		
		JVar outputRow = blockInfo.getTableRowVar(node);
		
		String fieldName = targetProperty.getPredicate().getLocalName();
		
		JBlock thenBlock = ifStatement==null ? blockInfo.getBlock() : ifStatement._then();
		
		thenBlock.add(outputRow.invoke("set").arg(JExpr.lit(fieldName)).arg(var));
		
		if (targetProperty.isRequired() && ifStatement!=null) {

			JVar errorBuilder = blockInfo.getErrorBuilderVar();
			String path = targetProperty.fullPath();
			String message = "Required property '" + path + "' is null";
			
			ifStatement._else().add(errorBuilder.invoke("addError").arg(JExpr.lit(message)));
		}

	}

	
	protected void fail(String pattern, Object...arguments) throws BeamTransformGenerationException {
		String msg = MessageFormat.format(pattern, arguments);
		throw new BeamTransformGenerationException(msg);
	}
	
	public BeamMethod generate(BeamMethod caller, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		String prefix = caller.name();
		String propertyName = targetProperty.getPredicate().getLocalName();
		
		
		StringBuilder builder = new StringBuilder();
		if (!"processElement".equals(prefix) && !"run".equals(prefix)) {
			builder.append(prefix);
			builder.append('_');
		}
		builder.append(propertyName);
		appendMethodNameSuffix(builder);
		
		
		RdfJavaType returnType = returnType(targetProperty);
		
	  
		String methodName = builder.toString();
		JMethod method = etran.getTargetClass().method(JMod.PRIVATE, returnType.getJavaType(), methodName);
		BeamMethod beamMethod = new BeamMethod(method);
		beamMethod.setTargetProperty(targetProperty);
		etran.beginBlock(beamMethod);
		try {

			beamMethod.setReturnType(returnType);
			
			addParameters(beamMethod, targetProperty);
			generateBody(beamMethod, targetProperty);
			
		} finally {
			etran.endBlock();
		}
		
		return beamMethod;
	}

	protected void appendMethodNameSuffix(StringBuilder builder) {
		// Do nothing
		// Subclasses may override
		
		
	}

	protected RdfJavaType returnType(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		return etran.getTypeManager().rdfJavaType(targetProperty);
	}
	
	


	abstract protected void generateBody(BeamMethod beamMethod, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException;

	abstract protected void addParameters(BeamMethod beamMethod, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException;
	
}

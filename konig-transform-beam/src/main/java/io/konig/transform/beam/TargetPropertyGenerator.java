package io.konig.transform.beam;

import java.text.MessageFormat;

import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlPropertyShape;

public abstract class TargetPropertyGenerator {

	protected BeamExpressionTransform etran;
	

	public TargetPropertyGenerator(BeamExpressionTransform etran) {
		this.etran = etran;
	}
	
	protected void captureValue(ShowlPropertyShape targetProperty, IJExpression ifTrue, JVar var) throws BeamTransformGenerationException {

		BlockInfo blockInfo = etran.peekBlockInfo();
		JConditional ifStatement = blockInfo.getBlock()._if(ifTrue);
		
		ShowlEffectiveNodeShape node = targetProperty.getDeclaringShape().effectiveNode();
		
		JVar outputRow = blockInfo.getTableRowVar(node);
		
		String fieldName = targetProperty.getPredicate().getLocalName();
		
		ifStatement._then().add(outputRow.invoke("set").arg(JExpr.lit(fieldName)).arg(var));
		
		if (targetProperty.isRequired()) {

			JVar errorBuilder = blockInfo.getErrorBuilderVar();
			String path = targetProperty.fullPath();
			String message = "Required property '" + path + "' is null";
			
			ifStatement._else().add(errorBuilder.invoke("addError").arg(JExpr.lit(message)));
		}

	}

	protected void captureValue(ShowlPropertyShape targetProperty, JVar var) throws BeamTransformGenerationException {
		captureValue(targetProperty, var.neNull(), var);
		etran.peekBlockInfo().getBlock()._return(var);
	}
	
	protected void fail(String pattern, Object...arguments) throws BeamTransformGenerationException {
		String msg = MessageFormat.format(pattern, arguments);
		throw new BeamTransformGenerationException(msg);
	}
	
	public BeamMethod generate(BeamMethod caller, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		String prefix = caller.name();
		String propertyName = targetProperty.getPredicate().getLocalName();
		
		
		StringBuilder builder = new StringBuilder();
		if (!"processElement".equals(prefix)) {
			builder.append(prefix);
			builder.append('_');
		}
		builder.append(propertyName);
		
		
		RdfJavaType returnType = returnType(targetProperty);
		
	  
		String methodName = builder.toString();
		JMethod method = etran.getTargetClass().method(JMod.PRIVATE, returnType.getJavaType(), methodName);
		BeamMethod beamMethod = new BeamMethod(method);
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

	protected RdfJavaType returnType(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		return etran.getTypeManager().rdfJavaType(targetProperty);
	}
	
	


	abstract protected void generateBody(BeamMethod beamMethod, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException;

	abstract protected void addParameters(BeamMethod beamMethod, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException;
	
}

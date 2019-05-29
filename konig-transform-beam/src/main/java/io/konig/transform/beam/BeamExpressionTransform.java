package io.konig.transform.beam;

import com.helger.jcodemodel.IJExpression;

import io.konig.core.showl.ShowlExpression;

public interface BeamExpressionTransform {
	
	IJExpression transform(ShowlExpression e) throws BeamTransformGenerationException;

}

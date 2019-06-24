package io.konig.transform.beam;

import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;

public interface BeamPropertySink {
	
	void captureProperty(BeamExpressionTransform etran, JConditional ifStatement, ShowlPropertyShape targetProperty, JVar propertyValue) 
			throws BeamTransformGenerationException;
		
}

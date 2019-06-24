package io.konig.transform.beam;

import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;

public class BeamListSink implements BeamPropertySink {
	
	private JVar listVar;


	public BeamListSink(JVar listVar) {
		this.listVar = listVar;
	}


	@Override
	public void captureProperty(BeamExpressionTransform etran, JConditional ifStatement,
			ShowlPropertyShape targetProperty, JVar propertyValue) throws BeamTransformGenerationException {
		
		
		ifStatement._then().add(listVar.invoke("add").arg(propertyValue));
		
		
	}



}

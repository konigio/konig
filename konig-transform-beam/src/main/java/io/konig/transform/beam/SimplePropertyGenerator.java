package io.konig.transform.beam;

import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;

public class SimplePropertyGenerator extends TargetPropertyGenerator {

	public SimplePropertyGenerator(BeamExpressionTransform etran) {
		super(etran);
	}

	

	@Override
	protected void addParameters(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		

		etran.addOutputRowAndErrorBuilderParams(beamMethod, targetProperty);
		etran.addTableRowParameters(beamMethod, targetProperty);
		
		
	}



	@Override
	protected void generateBody(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		
		
		JVar var = declareVariable(targetProperty);
		captureValue(targetProperty, var);
		
	}



	protected JVar declareVariable(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		return etran.declarePropertyValue(targetProperty);
	}


}

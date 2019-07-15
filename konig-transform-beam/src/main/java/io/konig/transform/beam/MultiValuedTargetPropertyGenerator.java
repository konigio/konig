package io.konig.transform.beam;

import io.konig.core.showl.ShowlArrayExpression;
import io.konig.core.showl.ShowlPropertyShape;

public class MultiValuedTargetPropertyGenerator extends AlternativePathsGenerator {

	public MultiValuedTargetPropertyGenerator(BeamExpressionTransform etran) {
		super(etran);
	}


	@Override
	protected void generateBody(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		

		ShowlArrayExpression e =  (ShowlArrayExpression) targetProperty.getSelectedExpression();
	
		

	}

}

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


import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.vocab.Konig;

public class SimplePropertyGenerator extends TargetPropertyGenerator {

	public SimplePropertyGenerator(BeamExpressionTransform etran) {
		super(etran);
	}

	

	@Override
	protected void addParameters(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		

		etran.addOutputRowAndErrorBuilderParams(beamMethod, targetProperty);
		etran.addTableRowParameters(beamMethod, targetProperty);
		etran.addPipelineOptionsParameters(beamMethod, targetProperty);
		
	}



	@Override
	protected void generateBody(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
		
		
		JVar var = declareVariable(targetProperty);
		captureValue(targetProperty, var);

		etran.peekBlockInfo().getBlock()._return(var);
		
	}



	protected JVar declareVariable(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		return etran.declarePropertyValue(targetProperty);
	}


}

package io.konig.transform.beam;

import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JExpr;

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
		
		if (etran.isOverlayPattern()) {
			generateOverlayBody(beamMethod, targetProperty);
		} else {

			JVar var = declareVariable(targetProperty);
			captureValue(targetProperty, var);

			etran.peekBlockInfo().getBlock()._return(var);
		}
		
		
	}



	private void generateOverlayBody(BeamMethod beamMethod, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {

		JVar result = etran.declareTargetPropertyValue(targetProperty);
		BlockInfo blockInfo = etran.peekBlockInfo();
		JBlock block = blockInfo.getBlock();
	
		block._if(result.neNull())._then()._return(result);
		
		block.assign(result, etran.transform(targetProperty.getSelectedExpression()));
		captureValue(targetProperty, result);
		
		block._return(result);
		
	}



	protected JVar declareVariable(ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		return etran.declarePropertyValue(targetProperty);
	}


}

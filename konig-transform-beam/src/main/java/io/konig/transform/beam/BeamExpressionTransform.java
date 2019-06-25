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


import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;

import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlExpression;

public interface BeamExpressionTransform {
	
	IJExpression transform(ShowlExpression e) throws BeamTransformGenerationException;

	BlockInfo beginBlock(JBlock block);

	void endBlock();

	BlockInfo peekBlockInfo() throws BeamTransformGenerationException;

	void processProperty(ShowlDirectPropertyShape targetProperty, ShowlExpression member) 
			throws BeamTransformGenerationException;
	
	void addRowParameters(BeamMethod beamMethod, ShowlExpression e) throws BeamTransformGenerationException;


}

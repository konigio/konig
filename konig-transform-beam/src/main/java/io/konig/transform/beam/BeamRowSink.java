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
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JStringLiteral;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;

public class BeamRowSink implements BeamPropertySink {
	
	public static BeamRowSink INSTANCE = new BeamRowSink();

	private BeamRowSink() {
	}

	@Override
	public void captureProperty(BeamExpressionTransform etran, JConditional ifStatement,
			ShowlPropertyShape targetProperty, IJExpression propertyValue) throws BeamTransformGenerationException {
		
		JVar outputRow = etran.peekBlockInfo().getOutputRow();
		
		if (outputRow == null) {
			throw new BeamTransformGenerationException("outputRow for property " + targetProperty.getPath() + " is null");
		}
		
		JStringLiteral fieldName = JExpr.lit(targetProperty.getPredicate().getLocalName());
		ifStatement._then().add(outputRow.invoke("set").arg(fieldName).arg(propertyValue));

	}

}

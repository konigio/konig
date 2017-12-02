package io.konig.transform.proto;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import static org.junit.Assert.assertTrue;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.shacl.Shape;
import io.konig.transform.factory.TransformTest;
import io.konig.transform.rule.ShapeRule;

public class AbstractShapeModelToShapeRuleTest extends TransformTest {

	protected OwlReasoner owlReasoner = new OwlReasoner(graph);
	protected ShapeModelFactory shapeModelFactory = new ShapeModelFactory(shapeManager, new BigQueryChannelFactory(), owlReasoner);
	protected ShapeModelToShapeRule shapeRuleFactory = new ShapeModelToShapeRule();

	protected void useBigQueryTransformStrategy() {
		
	}
	
	protected ShapeRule createShapeRule(URI shapeId) throws Exception {
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape != null);
		ShapeModel shapeModel = shapeModelFactory.createShapeModel(shape);
		
		return shapeRuleFactory.toShapeRule(shapeModel);
	}

}

package io.konig.transform.factory;

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

import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.transform.proto.ShapeModel;
import io.konig.transform.proto.ShapeModelFactory1;
import io.konig.transform.proto.ShapeModelToShapeRule;
import io.konig.transform.rule.ShapeRule;

public class ShapeRuleFactory {

	private ShapeManager shapeManager;
	private ShapeModelFactory1 shapeModelFactory;
	private ShapeModelToShapeRule shapeModelToShapeRule;


	public ShapeRuleFactory(ShapeManager shapeManager, ShapeModelFactory1 shapeModelFactory,
			ShapeModelToShapeRule shapeModelToShapeRule) {
		this.shapeManager = shapeManager;
		this.shapeModelFactory = shapeModelFactory;
		this.shapeModelToShapeRule = shapeModelToShapeRule;
	}




	public ShapeManager getShapeManager() {
		return shapeManager;
	}
	
	public ShapeModelToShapeRule getShapeModelToShapeRule() {
		return shapeModelToShapeRule;
	}

	public ShapeRule createShapeRule(Shape targetShape) throws TransformBuildException {

		try {
			ShapeModel shapeModel = shapeModelFactory.createShapeModel(targetShape);
			return shapeModelToShapeRule.toShapeRule(shapeModel);
		} catch (Throwable e) {
			throw new TransformBuildException(e);
		}
	}
}

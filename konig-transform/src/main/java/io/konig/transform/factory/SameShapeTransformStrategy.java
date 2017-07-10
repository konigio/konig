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


import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

/**
 * The simplest possible ShapeTransformStrategy which selects the source shape to be
 * identical to the target shape, except in the case of the root node.
 * @author Greg McFall
 *
 */
public class SameShapeTransformStrategy implements TransformStrategy {
	
	private ShapeRuleFactory factory;
	

	@Override
	public List<SourceShape> findCandidateSourceShapes(TargetShape target) throws TransformBuildException {
		Shape targetShape = target.getShape();
		List<SourceShape> result = new ArrayList<>();
		TargetProperty ta = target.getAccessor();
		if (ta==null) {
			ShapeManager shapeManager = factory.getShapeManager();
			URI targetClass = targetShape.getTargetClass();
			List<Shape> sourceList = shapeManager.getShapesByTargetClass(targetClass);
			
			for (Shape sourceShape : sourceList) {
				if (sourceShape == targetShape) {
					continue;
				}
				SourceShape source = SourceShape.create(sourceShape);
				target.match(source);
				
				result.add(source);
			}
		} else {
			SourceShape source = SourceShape.create(targetShape);
			SourceProperty sp = new SourceProperty(ta.getPropertyConstraint());
			sp.setParent(source);
			sp.setMatch(ta);
			sp.setNestedShape(source);
			target.match(source);
			ta.setPreferredMatch(sp);
			
			result.add(source);
		}
		return result;
	}


	@Override
	public void init(ShapeRuleFactory factory) {
		this.factory = factory;
	}

}

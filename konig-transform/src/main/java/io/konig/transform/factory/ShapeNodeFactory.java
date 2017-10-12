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


import java.util.Collection;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.KonigException;
import io.konig.core.Path;
import io.konig.core.path.HasStep;
import io.konig.core.path.HasStep.PredicateValuePair;
import io.konig.core.path.InStep;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyPath;
import io.konig.shacl.Shape;

abstract public class ShapeNodeFactory<S extends ShapeNode<P>, P extends PropertyNode<S>> {


	public S createShapeNode(Shape shape) {
		S shapeNode = shape(shape);
		addProperties(shapeNode, shape.getProperty(), false);
		return shapeNode;
	}
	
	protected void addProperties(S shapeNode, Collection<PropertyConstraint> propertyList, boolean isDerived) {

		for (PropertyConstraint p : propertyList) {
			PropertyPath path = p.getPath();
			if (path instanceof PredicatePath) {
				SharedSourceProperty preferredMatch = sharedSourceProperty(p);
				P propertyNode = property(p, -1, preferredMatch);
				propertyNode.setDerived(isDerived);
				shapeNode.add(propertyNode);
				handlePath(propertyNode, preferredMatch);
				handleValueShape(propertyNode);
			}
		}
	}

	private void handleValueShape(P propertyNode) {
		
		PropertyConstraint p = propertyNode.getPropertyConstraint();
		Shape valueShape = p.getShape();
		if (valueShape != null) {
			S nested = createShapeNode(valueShape);
			propertyNode.setNestedShape(nested);
		}
		
	}

	private void handlePath(P propertyNode, SharedSourceProperty preferredMatch) {
		PropertyConstraint p = propertyNode.getPropertyConstraint();
		Path path = p.getEquivalentPath();
		if (path != null) {
			
			List<Step> stepList = path.asList();
			P stepNode = null;
			S parent = propertyNode.getParent();
			int lastStep = stepList.size()-1;
			for (int i=0; i<stepList.size(); i++) {
				Step step = stepList.get(i);
				if (step instanceof OutStep) {
					
					if (i == 0) {
						parent = propertyNode.getParent();
					} else {
						parent = stepNode.getNestedShape();
						if (parent ==null) {
							parent = shape(null);
							stepNode.setNestedShape(parent);
						}
					}
					stepNode = property(p, i, i==lastStep ? preferredMatch : null);
					parent.add(stepNode);
				}
				if (step instanceof HasStep) {
					S container = stepNode.getNestedShape();
					if (container == null) {
						container = shape(null);
						stepNode.setNestedShape(container);
					}
					HasStep hasStep = (HasStep) step;
					for (PredicateValuePair pair : hasStep.getPairList()) {
						URI predicate = pair.getPredicate();
						Value value = pair.getValue();
						P pNode = property(p, i, predicate, value);
						container.add(pNode);
					}
				}
				if (step instanceof InStep) {
					throw new KonigException("In steps not supported");
				}
			}
		}
		
	}
	
	protected SharedSourceProperty sharedSourceProperty(PropertyConstraint p) {
		return null;
	}
	
	abstract protected S shape(Shape shape);
	
	abstract protected P property(PropertyConstraint p, int pathIndex, SharedSourceProperty preferredMatch);
	
	abstract protected P property(PropertyConstraint p, int pathIndex, URI predicate, Value value);


}

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


import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.KonigException;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyPath;
import io.konig.shacl.SequencePath;
import io.konig.shacl.Shape;

public class TargetShapeFactory extends ShapeNodeFactory<TargetShape, TargetProperty>  {
	
	
	public static final TargetShapeFactory INSTANCE = new TargetShapeFactory();
	
	public TargetShapeFactory() {
	}
	
	public TargetShape createShapeNode(Shape shape) {
		TargetShape target = super.createShapeNode(shape);
		addVariables(target);
		return target;
	}

	private void addVariables(TargetShape target) {
		Shape shape = target.getShape();
		List<PropertyConstraint> varList = shape.getVariable();
		if (varList != null) {
			for (PropertyConstraint p : varList) {
				target.addVariable(new VariableTargetProperty(p));
			}
		}
		
	}

	@Override
	protected TargetShape shape(Shape shape) {
		return new TargetShape(shape);
	}

	@Override
	protected TargetProperty property(PropertyConstraint p, int pathIndex, SharedSourceProperty preferredMatch) throws KonigException {
		if (pathIndex < 0) {
			

			PropertyPath path = p.getPath();
			if (path instanceof PredicatePath) {
			
				if (p.getFormula()==null) {
					return new BasicDirectTargetProperty(p);
				}
				return new DerivedDirectTargetProperty(p);
			}
			
			if (path instanceof SequencePath) {
				return new SequenceTargetProperty(p);
			} else {
				throw new KonigException("Unsupported property path type: " + (path == null ? null : path.getClass().getName()));
			}
			
		} else {
			if (preferredMatch==null) {
				return new ContainerIndirectTargetProperty(p, pathIndex);
			} else {
				return new LeafIndirectTargetProperty(p, pathIndex, preferredMatch);
			}
		}
	}

	@Override
	protected TargetProperty property(PropertyConstraint p, int pathIndex, URI predicate, Value value) {
		throw new KonigException("Value constraint not supported for property " + p.getPredicate());
	}

}

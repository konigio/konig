package io.konig.shacl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.formula.DirectedStep;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.QuantifiedExpression;

public class MemoryPropertyManager implements PropertyManager {
	
	private Map<URI,Set<ShapePropertyPair>> map = new HashMap<>();
	
	public MemoryPropertyManager(ShapeManager shapeManager) {
		scan(shapeManager);
	}
	
	public MemoryPropertyManager() {
		
	}
	
	public void scan(ShapeManager shapeManager) {

		for (Shape shape : shapeManager.listShapes()) {
			process(shape, shape.getProperty());
		}
	}

	private void process(Shape shape, List<PropertyConstraint> list) {
		for (PropertyConstraint p : list) {
			URI predicate = p.getPredicate();
			if (predicate != null) {
				put(predicate, shape, p);
			} 
				QuantifiedExpression formula = p.getFormula();
				if (formula != null) {
					formula.dispatch(new FormulaVisitor() {
						
						@Override
						public void exit(Formula formula) {
							
							if (formula instanceof DirectedStep) {
								DirectedStep direct = (DirectedStep) formula;
								put(direct.getTerm().getIri(), shape, p);
							}
						}
						
						@Override
						public void enter(Formula formula) {
							// Do nothing
						}
					});
				}
			
		}
		
	}

	private void put(URI predicate, Shape shape, PropertyConstraint p) {
		Set<ShapePropertyPair> set = map.get(predicate);
		if (set == null) {
			set = new HashSet<>();
			map.put(predicate, set);
		}
		set.add(new ShapePropertyPair(shape, p));
	}

	@Override
	public Set<ShapePropertyPair> propertyConstraintsByPathOrFormula(URI predicate) {
		
		return map.get(predicate);
	}

}

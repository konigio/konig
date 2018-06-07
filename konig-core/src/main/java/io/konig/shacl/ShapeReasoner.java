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


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class ShapeReasoner {
	private ShapeManager shapeManager;
	private Map<URI,Set<URI>> valueType;

	public ShapeReasoner(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}
	
	@SuppressWarnings("unchecked")
	public Set<URI> valueType(URI predicate) {
		if (valueType == null) {
			valueType = new HashMap<>();
			for (Shape shape : shapeManager.listShapes()) {
				for (PropertyConstraint p : shape.getProperty()) {
					URI property = p.getPredicate();
					if (property != null) {
						Set<URI> set = valueType.get(property);
						if (set == null) {
							set = new HashSet<>();
							valueType.put(predicate, set);
						}
						add(set, p.getDatatype());
						add(set, p.getValueClass());
						Shape valueShape = p.getShape();
						if (valueShape != null) {
							add(set, valueShape.getTargetClass());
						}
					}
				}
			}
		}

		Set<URI> result = valueType.get(predicate);
		if (result == null) {
			result = Collections.EMPTY_SET;
			valueType.put(predicate, result);
		}
		return result;
		
	}


	private void add(Set<URI> set, Resource element) {
		if (element instanceof URI) {
			set.add((URI)element);
		}
		
	}

}

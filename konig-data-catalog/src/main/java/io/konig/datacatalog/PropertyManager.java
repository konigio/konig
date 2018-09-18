package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.shacl.ClassStructure;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyStructure;
import io.konig.shacl.Shape;

public class PropertyManager {
	
	private Map<URI, PropertyUsage> map = new HashMap<>();
	
	public void build(ClassStructure classStructure) {
		for (PropertyStructure p : classStructure.listProperties()) {
			URI predicate = p.getPredicate();
			PropertyUsage usage = new PropertyUsage(predicate);
			for (Shape shape : p.getUsedInShape()) {
				PropertyConstraint constraint = shape.getPropertyConstraint(predicate);
				usage.add(constraint);
				map.put(predicate, usage);
			}
		}
	}
	
	public PropertyUsage getPropertyUsage(URI predicate) {
		return map.get(predicate);
	}

}

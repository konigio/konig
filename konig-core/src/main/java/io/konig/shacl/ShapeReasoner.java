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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class ShapeReasoner {
	private ShapeManager shapeManager;
	private Map<URI,PropertyInfo> propertyInfo;

	public ShapeReasoner(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}
	
	public PropertyInfo getPropertyInfo(URI predicate) {
		collectPropertyInfo();
		return propertyInfo.get(predicate);
	}
	
	public Set<URI> valueType(URI predicate) {
		
		collectPropertyInfo();

		PropertyInfo result = propertyInfo.get(predicate);
		if (result == null) {
			result = new PropertyInfo(predicate);
			propertyInfo.put(predicate, result);
		}
		return result.getValueTypes();
		
	}
	

	/**
	 * Return the set of predicates referenced by all known PropertyShape instances.
	 */
	public Set<URI> predicates() {
		collectPropertyInfo();
		return propertyInfo.keySet();
	}

	private void collectPropertyInfo() {
		if (propertyInfo == null) {
			propertyInfo = new HashMap<>();
			for (Shape shape : shapeManager.listShapes()) {
				for (PropertyConstraint p : shape.getProperty()) {
					URI property = p.getPredicate();
					if (property != null) {
						PropertyInfo info = propertyInfo.get(property);
						if (info == null) {
							info = new PropertyInfo(property);
							propertyInfo.put(property, info);
						}
						add(info.getValueTypes(), p.getDatatype());
						add(info.getValueTypes(), p.getValueClass());
						info.addUsage(new ShapePropertyPair(shape, p));
						Shape valueShape = p.getShape();
						if (valueShape != null) {
							add(info.getValueTypes(), valueShape.getTargetClass());
						}
					}
				}
			}
		}
		
	}

	private void add(Set<URI> set, Resource element) {
		if (element instanceof URI) {
			set.add((URI)element);
		}
		
	}
	
	public static class PropertyInfo {
		private URI predicate;
		private List<ShapePropertyPair> usage = new ArrayList<>();
		private Set<URI> values = new HashSet<>();
		
		public PropertyInfo(URI predicate) {
			this.predicate = predicate;
		}
		
		public void addUsage(ShapePropertyPair pair) {
			usage.add(pair);
		}
		
		public void addValue(URI valueType) {
			values.add(valueType);
		}

		public URI getPredicate() {
			return predicate;
		}

		public List<ShapePropertyPair> getUsage() {
			return usage;
		}

		public Set<URI> getValueTypes() {
			return values;
		}
		
		
	}
	
	

}

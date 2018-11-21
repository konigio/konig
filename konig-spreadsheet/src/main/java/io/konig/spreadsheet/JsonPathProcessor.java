package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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


import java.io.IOException;
import java.io.StringReader;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.jsonpath.JsonPath;
import io.konig.core.jsonpath.JsonPathField;
import io.konig.core.jsonpath.JsonPathOperator;
import io.konig.core.jsonpath.JsonPathParseException;
import io.konig.core.jsonpath.JsonPathParser;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class JsonPathProcessor {
	private ShapeManager shapeManager;
	private String propertyNamespace;
	
	
	
	public JsonPathProcessor(ShapeManager shapeManager, String propertyNamespace) {
		this.shapeManager = shapeManager;
		this.propertyNamespace = propertyNamespace;
	}

	public PropertyConstraint parse(Shape shape, String jsonPath) throws JsonPathParseException, IOException {
		
		
		String shapeIdBase=null;
		String shapeIdSuffix=null;
		
		JsonPathParser parser = new JsonPathParser();
		StringReader input = new StringReader(jsonPath);
		JsonPath path = parser.parse(input);
		
		if (path.isEmpty()) {
			throw new JsonPathParseException("Empty path");
		}
		
		if (!path.get(0).isRoot()) {
			throw new JsonPathParseException("Expected a path that starts with '$'");
		}
		
		PropertyConstraint constraint = null;
		Shape targetShape = shape;
		for (int i=1; i<path.size(); i++) {
			JsonPathOperator jop = path.get(i);
			if (!jop.isField()) {
				throw new JsonPathParseException("Expected a field for operator[" + i + "] in " + jsonPath);
			}
			JsonPathField field = jop.asField();
			
			if (!field.getFieldName().isName()) {
				throw new JsonPathParseException("Expected a name for field operator[" + i + "] in " + jsonPath);
			}
			
			String fieldName = field.getFieldName().asName().getValue();
			
			URI predicate = new URIImpl(propertyNamespace + fieldName);
			PropertyConstraint p = targetShape.getPropertyConstraint(predicate);
			if (p == null) {
				p = new PropertyConstraint(predicate);
				targetShape.add(p);
			}
			
			constraint = p;

			int next = i+1;
			Integer maxCount =  (next < path.size() && path.get(next).isBracket())  ?
					null : 1;
			
			p.setMaxCount(maxCount);
			if (maxCount == null) {
				i++;
			}
			
			if (hasNextField(path, i+1)) {
				if (shapeIdBase == null) {
					shapeIdBase = shape.getId().stringValue();
					if (shapeIdBase.endsWith("Shape")) {
						shapeIdBase = shapeIdBase.substring(0,  shapeIdBase.length()-5);
						shapeIdSuffix = "Shape";
					} else {
						shapeIdSuffix = "";
					}
				}
				
				URI nextShapeId = new URIImpl(shapeIdBase + '.' + fieldName + shapeIdSuffix);
				targetShape = shapeManager.getShapeById(nextShapeId);
				if (targetShape == null) {
					targetShape = new Shape(nextShapeId);
					shapeManager.addShape(targetShape);
				}
			}
			
		}
		
		
		return constraint;
	}

	private boolean hasNextField(JsonPath path, int start) throws JsonPathParseException {
		for (int i=start; i<path.size(); i++) {
			JsonPathOperator jop = path.get(i);
			if (jop.isField()) {
				return true;
			}
		}
		return false;
	}

}

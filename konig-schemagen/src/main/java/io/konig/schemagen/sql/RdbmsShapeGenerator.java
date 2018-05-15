package io.konig.schemagen.sql;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.shacl.PropertyConstraint;

/*
 * #%L
 * Konig Schema Generator
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


import io.konig.shacl.Shape;

public class RdbmsShapeGenerator {
	
	public Shape createRdbmsShape(Shape shape){
	if ( validateLocalNames(shape) == null){
		shape = null;
	}
		return shape;
		
	}


	public String validateLocalNames(Shape shape) {
		String propertyId = "";
		Shape clonedShape = shape.deepClone(); 
		for (PropertyConstraint p : clonedShape.getProperty()) {
			String fullURI =p.getPath().toString();
			int i = fullURI.lastIndexOf("/")+1;
			int j = fullURI.lastIndexOf(">");
			propertyId = fullURI.substring(i, j);
			propertyId = changeToSnakeCase(propertyId);
			fullURI = fullURI.substring(0,fullURI.lastIndexOf("/")+1);
			URI path = new URIImpl(stringUtilities(fullURI,propertyId)) ;
			p.setPath(path);
		}
		return propertyId;
		
	}
	

	public String changeToSnakeCase(String propertyId) {
		String camelCasePattern = "([a-z]+[A-Z]+\\w+)+"; 
		String snakeCasePatternLowerCase ="([a-z]+(?:_[a-z]+)*)";
		String snakeCasePatternUpperCase ="([A-Z]+(?:_[A-Z]+)*)";
		if(propertyId.matches(camelCasePattern)){
			propertyId = propertyId.replaceAll("([^_A-Z])([A-Z])", "$1_$2").toUpperCase();
		}else if(propertyId.matches(snakeCasePatternLowerCase)){
			propertyId = propertyId.replaceAll("([^_A-Z])([A-Z])", "$1_$2").toUpperCase();
		} else if(propertyId.matches(snakeCasePatternUpperCase)){
			propertyId = null;
		}
		return propertyId;
	}
	
	public String stringUtilities(String value, String val){
		StringBuffer sb = new StringBuffer();
		sb.append(value);
		sb.append(val);
		sb.append(">");
		
		return sb.toString();
		
	}

}

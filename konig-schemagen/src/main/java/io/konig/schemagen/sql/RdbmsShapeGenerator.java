package io.konig.schemagen.sql;


import java.util.ArrayList;
import java.util.List;

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
	
	private String shapeIriPattern;
	private String shapeIriReplacement;
	
	public RdbmsShapeGenerator(String shapeIriPattern,String shapeIriReplacement){
		this.shapeIriPattern=shapeIriPattern;
		this.shapeIriReplacement=shapeIriReplacement;
	}
	public String getShapeIriPattern() {
		return shapeIriPattern;
	}
	public void setShapeIriPattern(String shapeIriPattern) {
		this.shapeIriPattern = shapeIriPattern;
	}
	public String getShapeIriReplacement() {
		return shapeIriReplacement;
	}
	public void setShapeIriReplacement(String shapeIriReplacement) {
		this.shapeIriReplacement = shapeIriReplacement;
	}
	public Shape createRdbmsShape(Shape shape){
		return validateLocalNames(shape);		
	}


	public Shape validateLocalNames(Shape shape) {
		String propertyId = "";
		Shape clonedShape = shape.deepClone(); 
		List<PropertyConstraint> propConList=new ArrayList<PropertyConstraint>();
		boolean isEdited=false;
		for (PropertyConstraint p : clonedShape.getProperty()) {
			String fullURI =p.getPath().toString();
			int i = fullURI.lastIndexOf("/")+1;
			int j = fullURI.lastIndexOf(">");
			propertyId = fullURI.substring(i, j);
			String changedPropertyId = changeToSnakeCase(propertyId);
			if(!changedPropertyId.equals(propertyId) && !isEdited){
				isEdited=true;
			}
			fullURI = fullURI.substring(1,fullURI.lastIndexOf("/")+1);
			URI path = new URIImpl(stringUtilities(fullURI,changedPropertyId)) ;
			p.setPath(path);
			propConList.add(p);
			
		}
		clonedShape.setProperty(propConList);
		if(isEdited)			
			return clonedShape;
		else
			return null;		
	}
	

	public String changeToSnakeCase(String propertyId) {
		String camelCasePattern = "([a-z]+[A-Z]+\\w+)+"; 
		String pascalCasePattern = "([A-Z][a-z]+\\w+)+";
		String snakeCasePatternLowerCase ="([a-z]+(?:_[a-z]+)*)";
		String snakeCasePatternUpperCase ="([A-Z]+(?:_[A-Z]+)*)";
		if(propertyId.matches(camelCasePattern) || propertyId.matches(pascalCasePattern)){
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
		return sb.toString();
		
	}



}

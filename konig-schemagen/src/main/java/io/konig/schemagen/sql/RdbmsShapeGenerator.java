package io.konig.schemagen.sql;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.path.NamespaceMapAdapter;
import io.konig.formula.FormulaParser;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.ShapePropertyOracle;
import io.konig.rio.turtle.NamespaceMap;
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
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeLoader;

public class RdbmsShapeGenerator {
	
	private String shapeIriPattern;
	private String shapeIriReplacement;
	private String propertyNameSpace;
	private ShapeFileGetter fileGetter;
	public RdbmsShapeGenerator(String shapeIriPattern,String shapeIriReplacement, String propertyNameSpace){
		this.shapeIriPattern=shapeIriPattern;
		this.shapeIriReplacement=shapeIriReplacement;
		this.propertyNameSpace = propertyNameSpace;
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
	public String getPropertyNameSpace() {
		return propertyNameSpace;
	}
	public void setPropertyNameSpace(String propertyNameSpace) {
		this.propertyNameSpace = propertyNameSpace;
	}
	public Shape createRdbmsShape(Shape shape) throws RDFParseException, IOException{
		return validateLocalNames(shape);		
	}


	public Shape validateLocalNames(Shape shape) throws RDFParseException, IOException {
		String propertyId = "";
		Shape clonedShape = shape.deepClone(); 
		List<PropertyConstraint> propConList=new ArrayList<PropertyConstraint>();
		boolean isEdited=false;
		URI path = null;
		for (PropertyConstraint p : clonedShape.getProperty()) {
			String fullURI =p.getPath().toString();
			int i = fullURI.lastIndexOf("/")+1;
			int j = fullURI.lastIndexOf(">");
			propertyId = fullURI.substring(i, j);
			/*String formula =propertyId;
			try {
				p.setFormula(formulaParser(clonedShape,formula));
			} catch (RDFHandlerException e) {
				e.printStackTrace();
			}*/
			String changedPropertyId = changeToSnakeCase(propertyId);
			if(changedPropertyId!=null && !changedPropertyId.equals(propertyId) && !isEdited){
				isEdited=true;
			}
			fullURI = fullURI.substring(1,fullURI.lastIndexOf("/")+1);
				path = new URIImpl(propertyNameSpace+changedPropertyId) ;
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
public QuantifiedExpression formulaParser(Shape shape, String formula) throws RDFParseException, IOException, RDFHandlerException{
	Graph tbox = new MemoryGraph();
	NamespaceManager nsManager = new MemoryNamespaceManager();
	tbox.setNamespaceManager(nsManager);
	SimpleLocalNameService localNameService = new SimpleLocalNameService();
	localNameService.addAll(tbox);
	tbox.vertex(shape.getId());
	NamespaceMap nsMap = new NamespaceMapAdapter(tbox.getNamespaceManager());
	ShapePropertyOracle oracle = new ShapePropertyOracle();
		
	FormulaParser parser = new FormulaParser(oracle, localNameService, nsMap);

	// For a given `Shape` and a string representation of the formula, 
	// you can parse the formula like this...

	oracle.setShape(shape);
	QuantifiedExpression e = parser.quantifiedExpression(formula);
	return e;
	
	
}


}

package io.konig.schemagen.sql;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.path.NamespaceMapAdapter;
import io.konig.core.util.StringUtil;
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

public class RdbmsShapeGenerator {
	
	private String shapeIriPattern;
	private String shapeIriReplacement;
	private String propertyNameSpace;
	private FormulaParser parser;
	
	public RdbmsShapeGenerator(FormulaParser parser, String shapeIriPattern,String shapeIriReplacement, String propertyNameSpace) {
		this.shapeIriPattern=shapeIriPattern;
		this.shapeIriReplacement=shapeIriReplacement;
		this.propertyNameSpace = propertyNameSpace;
		this.parser = new FormulaParser();
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
		Shape clonedShape = shape.deepClone(); 
		beginShape(clonedShape);
		boolean isEdited=false;
		for (PropertyConstraint p : clonedShape.getProperty()) {
			URI predicate = p.getPredicate();
			if (predicate != null) {
				String localName = predicate.getLocalName();
				String snakeCase = StringUtil.SNAKE_CASE(localName);
				
				if (!localName.equals(snakeCase)) {
					isEdited = true;
					URI newPredicate =  new URIImpl(propertyNameSpace + snakeCase);
					declarePredicate(newPredicate);
					p.setPredicate(newPredicate);
					
					if (p.getFormula()==null) {
						String text = "." + localName;
						QuantifiedExpression formula = parser.quantifiedExpression(text);
						p.setFormula(formula);
					}
				}
			}
			
		}
		if(isEdited)			
			return clonedShape;
		else
			return null;		
	}
	
	
	private void declarePredicate(URI predicate) {
		if (parser.getLocalNameService() instanceof SimpleLocalNameService) {
			SimpleLocalNameService service = (SimpleLocalNameService) parser.getLocalNameService();
			service.add(predicate);
		}
		
	}
	private void beginShape(Shape shape) {
		if (parser.getPropertyOracle() instanceof ShapePropertyOracle) {
			ShapePropertyOracle oracle = (ShapePropertyOracle) parser.getPropertyOracle();
			oracle.setShape(shape);
		}
		
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

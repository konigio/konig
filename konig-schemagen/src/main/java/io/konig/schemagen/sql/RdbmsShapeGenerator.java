package io.konig.schemagen.sql;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

	public Shape createRdbmsShape(Shape shape) throws RDFParseException, IOException{
		
		return flattenNestedShape(shape,null,null);
	}


	private Shape flattenNestedShape(Shape shape,String propertyId,String fullURI) throws RDFParseException, IOException {
		String propertyId1=null;
		//Convert to snake case
		Shape rdbmsShape= validateLocalNames(shape);
		Shape clonedShape=null;
		if(rdbmsShape!=null){
			clonedShape = rdbmsShape.deepClone(); 
			ListIterator<PropertyConstraint> iterator=clonedShape.getProperty().listIterator();
			List<PropertyConstraint> propConstraints=new ArrayList<PropertyConstraint>();
			//Iterate the properties of the root shape.
			while(iterator.hasNext()){
				PropertyConstraint p=iterator.next();
				String fullURI1 =p.getPath().toString();
				int i = fullURI1.lastIndexOf("/")+1;
				int j = fullURI1.lastIndexOf(">");
				propertyId1 = fullURI1.substring(i, j);
				String changedPropertyId=null;
				if(p.getShape()!=null){					
					//Root shape - Property has a nested shape
					if(propertyId!=null && fullURI!=null)
						changedPropertyId=propertyId+"__"+propertyId1;
					else if(propertyId==null && fullURI==null)
						changedPropertyId=propertyId1;
					Shape flattenedNestedShape=flattenNestedShape(p.getShape(),propertyId1,fullURI1);
					propConstraints.addAll(flattenedNestedShape.getProperty());
				}
				else if(propertyId!=null && fullURI!=null){						
					//Nested shape - Property does not have a nested shape.
					changedPropertyId=propertyId+"__"+propertyId1;
					fullURI1 = fullURI.substring(1,fullURI.lastIndexOf("/")+1);
					URI path = new URIImpl(stringUtilities(fullURI1,changedPropertyId)) ;
					p.setPath(path);
					propConstraints.add(p);	
				}
				
				if(!propConstraints.isEmpty())
					iterator.remove();
			}			
			
			if(!propConstraints.isEmpty()){
				for(PropertyConstraint p2:propConstraints){
					clonedShape.add(p2);
				}
			}
		}
	
		return clonedShape;
	}
	
	public String getPropertyNameSpace() {
		return propertyNameSpace;
	}
	public void setPropertyNameSpace(String propertyNameSpace) {
		this.propertyNameSpace = propertyNameSpace;
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

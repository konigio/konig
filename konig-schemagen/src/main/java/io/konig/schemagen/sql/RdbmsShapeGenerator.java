package io.konig.schemagen.sql;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.aws.datasource.AwsAurora;
import io.konig.aws.datasource.AwsAuroraTable;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.path.NamespaceMapAdapter;
import io.konig.core.util.StringUtil;
import io.konig.datasource.DataSource;
import io.konig.formula.FormulaParser;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.ShapePropertyOracle;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
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
		
		return flattenNestedShape(shape,null,null,null);
	}


	private Shape flattenNestedShape(Shape shape,String propertyId,String formula,PropertyConstraint parentProperty) throws RDFParseException, IOException {
		String propertyId1=null;
		Shape rdbmsShape= validateLocalNames(shape);
		if(rdbmsShape!=null){
				if(propertyId!=null || isValidRdbmsShape(rdbmsShape)){
				beginShape(rdbmsShape);			
				ListIterator<PropertyConstraint> iterator=rdbmsShape.getProperty().listIterator();
				List<PropertyConstraint> propConstraints=new ArrayList<PropertyConstraint>();
				while(iterator.hasNext()){
					PropertyConstraint p=iterator.next();				
					URI predicate =p.getPredicate();
					String formula1=p.getFormula().toSimpleString();
					propertyId1=predicate.getLocalName();
					String changedPropertyId=null;
					String formulaText=null;
					if(p.getShape()!=null){
						if(p.getMaxCount()==1){
							changedPropertyId=(propertyId==null)?propertyId1:propertyId+"__"+propertyId1;	
							formulaText = (propertyId==null)?formula1:formula+formula1;
							Shape flattenedNestedShape=flattenNestedShape(p.getShape(),changedPropertyId,formulaText,p);
							propConstraints.addAll(flattenedNestedShape.getProperty());
						}
						else
							p.setShape(null);
					}
					else if(propertyId!=null){		
						changedPropertyId=propertyId+"__"+propertyId1;
						formulaText= formula+formula1;
						if(parentProperty.getMinCount()==0){
							p.setMinCount(0);
						}
						URI path = new URIImpl(stringUtilities(parentProperty.getPredicate().getNamespace(),changedPropertyId)) ;
						p.setPath(path);
						QuantifiedExpression formulaExp = parser.quantifiedExpression(formulaText);
						p.setFormula(formulaExp);
						propConstraints.add(p);	
					}
					
					if(!propConstraints.isEmpty())
						iterator.remove();
				}
				if(!propConstraints.isEmpty()){
					for(PropertyConstraint p2:propConstraints){
						rdbmsShape.add(p2);
					}
				}
			}
		}
		return rdbmsShape;
	}
	
	private boolean isValidRdbmsShape(Shape rdbmsShape) {
		AwsAurora auroraTable = rdbmsShape.findDataSource(AwsAurora.class);
		GoogleCloudSqlTable gcpSqlTable = rdbmsShape.findDataSource(GoogleCloudSqlTable.class);
		if (auroraTable !=null || gcpSqlTable != null){
			return true;
		}
		return false;
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

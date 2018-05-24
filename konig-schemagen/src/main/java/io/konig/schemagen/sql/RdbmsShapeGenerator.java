package io.konig.schemagen.sql;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFParseException;

import io.konig.aws.datasource.AwsAurora;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.util.StringUtil;
import io.konig.formula.FormulaParser;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.ShapePropertyOracle;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
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
		Shape clone = null;
		if (accept(shape)) {
			clone = shape.deepClone();
			updateOracle(shape);
			process(clone, ".", "", clone);
		}
		return clone;
		
	}
	
	/**
	 * Check whether the shape contains any properties that need to be renamed using SNAKE_CASE, or any 
	 * nested shapes that need to be flattened, and that it is a valid shape that required RDBMS generation.
	 * 
	 * @param shape
	 * @return true if the supplied shape contains fields that need to be renamed or any nested shapes.
	 */
	private boolean accept(Shape shape) {
		
		if (!SqlTableGeneratorUtil.isValidRdbmsShape(shape)) {
			return false;
		}
		
		for (PropertyConstraint p : shape.getProperty()) {
			if (p.getShape() != null && p.getMaxCount()!=null && p.getMaxCount()==1) {
				return true;
			}
			if (p.getDatatype() != null) {
				URI predicate = p.getPredicate();
				if (predicate != null) {
					String localName = predicate.getLocalName();
					String snakeCase = StringUtil.SNAKE_CASE(localName);
					if (!localName.equals(snakeCase)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	private void process(Shape rdbmsShape, String logicalPath, String prefix, Shape propertyContainer) throws RDFParseException, IOException {
		
		List<PropertyConstraint> list = new ArrayList<>(propertyContainer.getProperty());
		
		for (PropertyConstraint p : list) {
			URI predicate = p.getPredicate();
			if (predicate != null && p.getMaxCount()!=null && p.getMaxCount()==1) {
				String localName = predicate.getLocalName();
				String snakeCase = StringUtil.SNAKE_CASE(localName);

				String newLocalName = prefix + snakeCase;
				
				if (p.getDatatype() != null && !localName.equals(snakeCase)) {
						
					URI newPredicate =  new URIImpl( propertyNameSpace + newLocalName );
					
					declarePredicate(newPredicate);
					p.setPredicate(newPredicate);
					
					if (p.getFormula()==null) {
						
						String text = logicalPath + localName;
						QuantifiedExpression formula = parser.quantifiedExpression(text);
						p.setFormula(formula);
					}
					
					if (propertyContainer != rdbmsShape) {
						rdbmsShape.add(p);
					}
						
				} else if (p.getShape() != null) {
					String nestedPath = logicalPath + localName + '.';
					String nestedPrefix = snakeCase + "__";
					
					process(rdbmsShape, nestedPath, nestedPrefix, p.getShape());
				}
				
			}
		}
		
	}
	
	private void declarePredicate(URI predicate) {
		if (parser.getLocalNameService() instanceof SimpleLocalNameService) {
			SimpleLocalNameService service = (SimpleLocalNameService) parser.getLocalNameService();
			service.add(predicate);
		}
		
	}
	private void updateOracle(Shape shape) {
		if (parser.getPropertyOracle() instanceof ShapePropertyOracle) {
			ShapePropertyOracle oracle = (ShapePropertyOracle) parser.getPropertyOracle();
			oracle.setShape(shape);
		}
		
	}


}

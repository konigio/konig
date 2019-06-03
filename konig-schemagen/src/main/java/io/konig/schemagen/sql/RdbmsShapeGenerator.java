package io.konig.schemagen.sql;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFParseException;

import io.konig.aws.datasource.AwsAurora;
import io.konig.core.Context;
import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.Term;
import io.konig.core.impl.BasicContext;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.LocalNameTerm;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.QuantifiedExpression;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.NodeKind;
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
	
	private String propertyNameSpace;
	private List<PropertyConstraint> rdbmsProperty = null;
	private OwlReasoner owlReasoner;
	private RdbmsShapeHelper rdbmsShapeHelper;
	
	public RdbmsShapeGenerator(OwlReasoner owlReasoner,RdbmsShapeHelper rdbmsShapeHelper) {
		this.owlReasoner = owlReasoner;
		this.rdbmsShapeHelper = rdbmsShapeHelper;
	}
	
	public RdbmsShapeGenerator(String propertyNameSpace, OwlReasoner owlReasoner) {
		this.propertyNameSpace = propertyNameSpace;
		this.owlReasoner = owlReasoner;
	}

	public Shape createOneToManyChildShape(Shape parentShape, URI relationshipProperty, Shape childShape) throws RDFParseException, IOException {
		throw new RuntimeException("createOneToManyChildShape not supported yet");
//		Shape rdbmsChildShape = createRdbmsShape(childShape);
//		if(rdbmsChildShape != null) {
//			addSyntheticKey(parentShape, "_FK", relationshipProperty);
//			rdbmsChildShape.setProperty(rdbmsProperty);
//		}
//		return rdbmsChildShape;
	}
	
	public Shape createRdbmsShape(Shape shape) throws RDFParseException, IOException{
		Shape clone = null;
		if (accept(shape)) {
			clone = shape.deepClone();
			rdbmsProperty = new ArrayList<>();
			process(clone, new FormulaBuilder(), "", clone.getTabularOriginShape());
			verifyPrimaryKeyCount(clone);		
		}
		return clone;
	}
	
	private void verifyPrimaryKeyCount(Shape clone) {
		int primaryKeyCount=0;
		for(PropertyConstraint pc:clone.getProperty()){
			if(Konig.primaryKey.equals(pc.getStereotype()) || Konig.syntheticKey.equals(pc.getStereotype())){
				primaryKeyCount++;					
			}
			if(primaryKeyCount>1)
				throw new KonigException("RDBMS Shape cannot have more than 1 primary key");
		}
	}

	/**
	 * Check whether the shape contains any properties that need to be renamed using SNAKE_CASE, or any 
	 * nested shapes that need to be flattened, and that it is a valid shape that required RDBMS generation.
	 * 
	 * @param shape
	 * @return true if the supplied shape contains fields that need to be renamed or any nested shapes.
	 */
	private boolean accept(Shape shape) {
		if (shape.getTabularOriginShape()==null  || !shape.getProperty().isEmpty() ) {
			return false;
		}
		
		for (PropertyConstraint p : shape.getTabularOriginShape().getProperty()) {
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
	private void process(Shape rdbmsShape, FormulaBuilder builder, String prefix, Shape propertyContainer) throws RDFParseException, IOException {
		List<PropertyConstraint> list = new ArrayList<>(propertyContainer.getProperty());
		
		for (PropertyConstraint p : list) {
			URI predicate = p.getPredicate();
			if(p.getShape() == null && (p.getDatatype() == null || p.getMaxCount() == null || p.getMaxCount() > 1) 
					&& rdbmsShape.getNodeKind() != NodeKind.IRI ) {
				
				String errorMessage = MessageFormat.format("\n In Shape {0}, the property {1} is ill-defined. \n"
						, new URIImpl(rdbmsShape.getId().toString()).getLocalName(), predicate.getLocalName());
				StringBuilder msg = new StringBuilder();
				msg.append(errorMessage);
				msg.append(" 1. Set sh:maxCount = 1 \n");
				msg.append(" 2. Set sh:shape so that it references a nested shape. \n");
				msg.append(" 3. Set sh:nodeKind equal to sh:IRI and specify a value for sh:class \n");
				msg.append(" 4. Set sh:datatype equal to one of the XML Schema datatype names.");
				throw new KonigException(msg.toString());
			}	
			builder.out(predicate);
			if (predicate != null && p.getMaxCount()!=null && p.getMaxCount()==1) {
				
				String localName = predicate.getLocalName();
				String snakeCase = StringUtil.SNAKE_CASE(localName);

				String newLocalName = prefix + snakeCase;
				
				if (p.getDatatype() != null && !localName.equals(snakeCase)) {
						
					URI newPredicate =  new URIImpl( propertyNameSpace + newLocalName );
					
					p.setPredicate(newPredicate);
					
					if (p.getFormula()==null) {	
						QuantifiedExpression formula = builder.build();
						p.setFormula(formula);
					}
					
					rdbmsProperty.add(p);	
				} else if (p.getShape() != null) {	
					String nestedPrefix = prefix + snakeCase + "__";
					process(rdbmsShape, builder, nestedPrefix, p.getShape());
				}
			}
			builder.pop();
			
			if(p.getShape() != null &&  p.getMaxCount()==null ) {
				
//				addSyntheticKey(rdbmsShape, "_PK", null);
			}
		}
		
		rdbmsShape.setPropertyList(rdbmsProperty);
	}
	
//	private void addSyntheticKey(Shape rdbmsShape, String suffix, URI relationshipProperty) throws RDFParseException, IOException {
//		Shape shape = rdbmsShape.getTabularOriginShape();
//		PropertyConstraint pc = hasPrimaryKey(shape);
//		String localName = "";
//		if(pc != null && "_PK".equals(suffix)) {
//			localName = StringUtil.SNAKE_CASE(pc.getPredicate().getLocalName());
//			URI newPredicate =  new URIImpl(propertyNameSpace + localName + suffix);
//			declarePredicate(newPredicate);
//			pc.setPredicate(newPredicate);
//			if(relationshipProperty != null){
//				
//				String text = "^" + relationshipProperty.getLocalName() + "."+ localName + "_PK" ;
//				pc.setFormula(parser.quantifiedExpression(text));
//			}
//		} else {
//			pc = createSyntheticKey(shape,suffix,relationshipProperty);
//		}
//		rdbmsProperty.add(pc);
//	}	
		
	
	private PropertyConstraint hasPrimaryKey(Shape rdbmsShape) {
		for (PropertyConstraint p : rdbmsShape.getProperty()) {
			if(p.getStereotype() != null && (p.getStereotype().equals(Konig.syntheticKey) 
					|| p.getStereotype().equals(Konig.primaryKey))){
				return p;
			}	
		}
		
		return null;
	}
	
	private PropertyConstraint createSyntheticKey(Shape rdbmsShape,String suffix, URI relationshipProperty) throws RDFParseException, IOException {
		PropertyConstraint pc = null;
		String localName = null;
		FormulaBuilder formula = new FormulaBuilder();
		if(owlReasoner != null && relationshipProperty != null) {
			Set<URI> inverseOf = owlReasoner.inverseOf(relationshipProperty);
			for(URI inverse : inverseOf) {
				pc = new PropertyConstraint(new URIImpl(propertyNameSpace + StringUtil.SNAKE_CASE(inverse.getLocalName()) + suffix));
				pc.setDatatype(XMLSchema.STRING);
				formula.out(inverse);
				pc.setFormula(formula.build());
			}
		}
		if (rdbmsShape.getNodeKind() == NodeKind.IRI && pc == null) {
			localName = StringUtil.SNAKE_CASE(Konig.id.getLocalName());
			pc = new PropertyConstraint(new URIImpl(Konig.id.getNamespace() + localName));
			pc.setDatatype(XMLSchema.STRING);
			if(relationshipProperty != null) {
				formula.in(relationshipProperty);
				pc.setFormula(formula.build());
			}
			
		} else if(pc == null){
			localName = StringUtil.SNAKE_CASE(rdbmsShape.getTargetClass().getLocalName());
			String snakeCase = localName + suffix;
			pc = new PropertyConstraint(new URIImpl(propertyNameSpace + snakeCase));
			pc.setDatatype(XMLSchema.LONG);			
			if(relationshipProperty != null) {
				formula.in(relationshipProperty).out(new URIImpl(propertyNameSpace+localName+"_PK"));
				pc.setFormula(formula.build());
			}
		}
		
		pc.setMaxCount(1);
		pc.setMinCount(1);
		if("_PK".equals(suffix)){
			pc.setStereotype(Konig.syntheticKey);
		}
		return pc;
	}
	
	
	
	private static class DirectedPredicate {
		private Direction direction;
		private URI predicate;
		public DirectedPredicate(Direction direction, URI predicate) {
			this.direction = direction;
			this.predicate = predicate;
		}
		public Direction getDirection() {
			return direction;
		}
		public URI getPredicate() {
			return predicate;
		}
		
		
	}
	
	private static class FormulaBuilder {
		private List<DirectedPredicate> predicateList = new ArrayList<>();
		
		
		
		FormulaBuilder out(URI predicate) {
			predicateList.add(new DirectedPredicate(Direction.OUT, predicate));
			return this;
		}
		
		FormulaBuilder in(URI predicate) {
			predicateList.add(new DirectedPredicate(Direction.IN, predicate));
			return this;
		}
		
		void pop() {
			predicateList.remove(predicateList.size()-1);
		}
		
		public QuantifiedExpression build() {
			PathExpression path = new PathExpression();
			QuantifiedExpression formula = QuantifiedExpression.wrap(path);
			Context context = new BasicContext(null);
			formula.setContext(context);
			for (DirectedPredicate e : predicateList) {
				URI predicate = e.getPredicate();
				context.add(new Term(predicate.getLocalName(), predicate.stringValue()));
				PathStep step = new DirectionStep(e.getDirection(), new LocalNameTerm(context, predicate.getLocalName()));
				path.add(step);
			}
			return formula;
		}
	}
	
	


}

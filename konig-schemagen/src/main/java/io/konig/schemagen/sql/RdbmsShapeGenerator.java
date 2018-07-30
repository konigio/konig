package io.konig.schemagen.sql;


import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFParseException;

import io.konig.activity.Activity;
import io.konig.aws.datasource.AwsAurora;
import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.formula.FormulaParser;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.ShapePropertyOracle;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.RelationshipDegree;

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
import io.konig.shacl.ShapeManager;

public class RdbmsShapeGenerator {
	
	private String propertyNameSpace;
	private FormulaParser parser;
	private List<PropertyConstraint> rdbmsProperty = null;
	private OwlReasoner owlReasoner;
	private ShapeManager shapeManager;
	public RdbmsShapeGenerator(FormulaParser parser, OwlReasoner owlReasoner, ShapeManager shapeManager) {
		this.parser = new FormulaParser();
		this.owlReasoner = owlReasoner;
		this.shapeManager=shapeManager;
	}
	public Shape createOneToManyChildShape(Shape parentShape, URI relationshipProperty, Shape childShape) throws RDFParseException, IOException {		
		Shape rdbmsChildShape = createRdbmsShape(childShape);
		declarePredicate(relationshipProperty);	
		if(rdbmsChildShape != null) {
			addSyntheticKey(parentShape, "_FK", relationshipProperty);
			rdbmsChildShape.setProperty(rdbmsProperty);
		}
		return rdbmsChildShape;
	}
	
	public Shape createRdbmsShape(Shape shape) throws RDFParseException, IOException{
		Shape clone = null;
		if (accept(shape)) {
			clone = shape.deepClone();
			updateOracle(shape);
			rdbmsProperty = new ArrayList<>();
			process(clone, ".", "", clone.getTabularOriginShape());
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
		if (!isValidRdbmsShape(shape)  || !shape.getProperty().isEmpty() ) {
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
					
					rdbmsProperty.add(p);	
				} else if (p.getShape() != null) {					
					String nestedPath = logicalPath + localName + '.';
					String nestedPrefix = prefix + snakeCase + "__";
					declarePredicate(p.getPredicate());
					process(rdbmsShape, nestedPath, nestedPrefix, p.getShape());
				}
			}
			
			if(p.getShape() != null &&  p.getMaxCount()==null ) {
				addSyntheticKey(rdbmsShape, "_PK", null);
			}
		}
		
		rdbmsShape.setProperty(rdbmsProperty);
	}
	
	private void addSyntheticKey(Shape rdbmsShape, String suffix, URI relationshipProperty) throws RDFParseException, IOException {
		Shape shape = rdbmsShape.getTabularOriginShape();
		PropertyConstraint pc = hasPrimaryKey(shape);
		String localName = "";
		if(pc != null && "_PK".equals(suffix)) {
			localName = StringUtil.SNAKE_CASE(pc.getPredicate().getLocalName());
			URI newPredicate =  new URIImpl(propertyNameSpace + localName + suffix);
			declarePredicate(newPredicate);
			pc.setPredicate(newPredicate);
			if(relationshipProperty != null){
				String text = "^" + relationshipProperty.getLocalName() + "."+ localName + "_PK" ;
				pc.setFormula(parser.quantifiedExpression(text));
			}
		} else {
			pc = createSyntheticKey(shape,suffix,relationshipProperty);
			rdbmsProperty.add(pc);
		}
	}	
		
	
	public PropertyConstraint hasPrimaryKey(Shape rdbmsShape) {
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
		String text = null;
		if(owlReasoner != null && relationshipProperty != null) {
			Set<URI> inverseOf = owlReasoner.inverseOf(relationshipProperty);
			for(URI inverse : inverseOf) {
				pc = new PropertyConstraint(new URIImpl(propertyNameSpace + StringUtil.SNAKE_CASE(inverse.getLocalName()) + suffix));
				pc.setDatatype(XMLSchema.STRING);
				text = "." + inverse.getLocalName();
				declarePredicate(inverse);
				pc.setFormula(parser.quantifiedExpression(text));
			}
		}
		if (rdbmsShape.getNodeKind() == NodeKind.IRI && pc == null) {
			localName = StringUtil.SNAKE_CASE(Konig.id.getLocalName());
			pc = new PropertyConstraint(new URIImpl(Konig.id.getNamespace() + localName));
			pc.setDatatype(XMLSchema.STRING);
			if(relationshipProperty != null) {
			text = "^" + relationshipProperty.getLocalName();
			declarePredicate(pc.getPredicate());
			pc.setFormula(parser.quantifiedExpression(text));
			}
			
		} else if(pc == null){
			localName = StringUtil.SNAKE_CASE(rdbmsShape.getTargetClass().getLocalName());
			String snakeCase = localName + suffix;
			pc = new PropertyConstraint(new URIImpl(propertyNameSpace + snakeCase));
			pc.setDatatype(XMLSchema.LONG);			
			if(relationshipProperty != null) {
				text = "^" + relationshipProperty.getLocalName() + "."+ localName + "_PK" ;
				declarePredicate(new URIImpl(propertyNameSpace+localName+"_PK"));
				pc.setFormula(parser.quantifiedExpression(text));
			}
		}
		
		pc.setMaxCount(1);
		pc.setMinCount(1);
		if("_PK".equals(suffix)){
			pc.setStereotype(Konig.syntheticKey);
		}
		return pc;
	}
	
	private boolean isValidRdbmsShape(Shape rdbmsShape) {
		AwsAurora auroraTable = rdbmsShape.findDataSource(AwsAurora.class);
		GoogleCloudSqlTable gcpSqlTable = rdbmsShape.findDataSource(GoogleCloudSqlTable.class);
		if (auroraTable !=null ){
			propertyNameSpace = auroraTable.getTabularFieldNamespace();
			return true;
		}
		if(gcpSqlTable!=null){
			propertyNameSpace = gcpSqlTable.getTabularFieldNamespace();
			return true;
		}
		return false;
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
	public List<Shape> createManyToManyChildShape(Shape parentShape, PropertyConstraint relationshipPc, Shape childShape) throws RDFParseException, IOException {
		List<Shape> manyToManyShapes=null;
		//1. Create Child RDBMS Shape
		Shape rdbmsChildShape = createRdbmsShape(childShape);
		if(rdbmsChildShape != null) {
			//2. Add Primary key to the child shape if not exists. Primary key will be added to the parent shape in the calling method itself
			addSyntheticKey(rdbmsChildShape, "_PK", null);
			rdbmsChildShape.setProperty(rdbmsProperty);			
			manyToManyShapes=new ArrayList<Shape>();
			//3. Create association shape
			Shape assocShape=getAssociationShape(parentShape,relationshipPc,childShape);
			if(assocShape!=null){
				//4. Add both association shape and child shape.
				manyToManyShapes.add(assocShape);
				manyToManyShapes.add(rdbmsChildShape);
			}
		}
		return manyToManyShapes;
	}
	private Shape getAssociationShape(Shape parentShape, PropertyConstraint relationshipPc, Shape childShape) throws RDFParseException, IOException {	
		
		boolean hasAssocShape=false;
		//boolean hasAssocShape=associationShapeExists(parentShape, childShape);
		Shape assocShape =null;
		if(!hasAssocShape){
			URI assocShapeId=getAssocShapeId((URI)(parentShape.getId()),(URI)(childShape.getId()));
			assocShape = shapeManager.getShapeById(assocShapeId);
			if (assocShape == null) {
				assocShape = new Shape(assocShapeId);
				shapeManager.addShape(assocShape);
				assocShape.addType(SH.Shape);
				assocShape.addType(Konig.TabularNodeShape);
				assocShape.setTargetClass(parentShape.getTabularOriginShape().getTargetClass());
				URI activityId = Activity.nextActivityId();
				Activity shapeGen = new Activity(activityId);
				shapeGen.setType(Konig.AssociationShape);
				shapeGen.setEndTime(GregorianCalendar.getInstance());
				assocShape.setWasGeneratedBy(shapeGen);
				rdbmsProperty = new ArrayList<>();
			}
			if(parentShape.getTabularOriginShape().getNodeKind() == NodeKind.IRI){
				assocShape.setNodeKind(NodeKind.IRI);
			}
			else{
				addSyntheticKeyToAssocShape(parentShape,"_FK", null);
			}
			addSyntheticKeyToAssocShape(childShape,"_FK", relationshipPc);
			assocShape.setProperty(rdbmsProperty);				
		}
		
		return assocShape;
	}
	private void addSyntheticKeyToAssocShape(Shape rdbmsShape, String suffix, PropertyConstraint relationshipPc) throws RDFParseException, IOException {
		Shape shape = rdbmsShape.getTabularOriginShape();
		PropertyConstraint pc = null;
		String localName = null;
		String text = null;
		
		if(owlReasoner != null && relationshipPc!=null && relationshipPc.getPredicate()!= null) {
			Set<URI> inverseOf = owlReasoner.inverseOf(relationshipPc.getPredicate());
			for(URI inverse : inverseOf) {
				pc = new PropertyConstraint(new URIImpl(propertyNameSpace + StringUtil.SNAKE_CASE(inverse.getLocalName()) + suffix));
				pc.setDatatype(XMLSchema.STRING);
				text = "." + inverse.getLocalName();
				declarePredicate(inverse);
				pc.setFormula(parser.quantifiedExpression(text));
			}
		}
		if (shape.getNodeKind() == NodeKind.IRI && pc == null) {
			localName = StringUtil.SNAKE_CASE(relationshipPc.getPredicate().getLocalName());
			pc = new PropertyConstraint(new URIImpl(propertyNameSpace + localName));
			pc.setValueClass(relationshipPc.getShape().getTargetClass());
			pc.setNodeKind(NodeKind.IRI);
			text = "." + relationshipPc.getPredicate().getLocalName();
			declarePredicate(pc.getPredicate());
			pc.setFormula(parser.quantifiedExpression(text));
			
		} else if(pc == null){
			localName = StringUtil.SNAKE_CASE(rdbmsShape.getTargetClass().getLocalName());
			String snakeCase = localName + suffix;
			pc = new PropertyConstraint(new URIImpl(propertyNameSpace + snakeCase));
			pc.setDatatype(XMLSchema.LONG);			
			if(relationshipPc != null) {
				text = "." + relationshipPc.getPredicate().getLocalName() + "."+ localName + "_PK" ;				
			}
			else{
				text = "."+ localName + "_PK" ;
			}
			declarePredicate(new URIImpl(propertyNameSpace+localName+"_PK"));
			pc.setFormula(parser.quantifiedExpression(text));
		}
		
		pc.setMaxCount(1);
		pc.setMinCount(1);
		if("_PK".equals(suffix)){
			pc.setStereotype(Konig.syntheticKey);
		}
		rdbmsProperty.add(pc);
	
	}
	private URI getAssocShapeId(URI parentShapeId, URI childShapeId) {
		URI assocShapeId=null;
		String namespace=parentShapeId.getNamespace();
		String parentShapeName=parentShapeId.getLocalName();
		String childShapeName=childShapeId.getLocalName();
		if(parentShapeName.indexOf("Shape")!=-1){
			parentShapeName=parentShapeName.substring(0, parentShapeName.indexOf("Shape"));			
		}
		if(childShapeName.indexOf("Shape")!=-1){
			childShapeName=childShapeName.substring(0, childShapeName.indexOf("Shape"));		
		}
		assocShapeId=new URIImpl(namespace+parentShapeName+childShapeName+"Shape");
		return assocShapeId;
	}
	
	/*public PropertyConstraint hasIRIProperty(Shape shape) {
		for (PropertyConstraint p : shape.getProperty()) {
			if(p.getNodeKind() == NodeKind.IRI){
				return p;
			}	
		}
		return null;
	}
	private boolean associationShapeExists(Shape parentShape,Shape childShape) {
		PropertyConstraint parentPk=hasPrimaryKey(parentShape);
		PropertyConstraint parentIri=hasIRIProperty(parentShape);
		PropertyConstraint childPk=hasPrimaryKey(parentShape);
		PropertyConstraint childIri=hasIRIProperty(parentShape);
		if((parentPk!=null || parentIri!=null) && (childPk!=null || childIri!=null)){
			for(Shape shape:shapeManager.listShapes()){
				if((shape.hasPropertyConstraint(parentPk.getPredicate()) || shape.hasPropertyConstraint(parentIri.getPredicate()))
						&& (shape.hasPropertyConstraint(childPk.getPredicate()) || shape.hasPropertyConstraint(childIri.getPredicate()))){
							return true;
						}
			}
		}
		return false;
	}*/
	


}

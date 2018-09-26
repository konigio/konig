package io.konig.schemagen.sql;

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


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.util.StringUtil;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.formula.FormulaBuilder;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class TabularShapeFactory {
	
	private static enum PropertyType {
		OneLiteral,
		ManyLiterals,
		OneObjectValue,
		ManyObjectValues,
		OneObjectReference,
		ManyObjectReferences
	}

	private ShapeManager shapeManager;
	private TabularShapeNamer namer = new DefaultTabularShapeNamer();
	private String tabularPropertyNamespace;
	private TabularLinkingStrategy linkingStrategy;


	public TabularShapeFactory(ShapeManager shapeManager, String tabularPropertyNamespace) {
		this(shapeManager, tabularPropertyNamespace, new SyntheticKeyLinkingStrategy(tabularPropertyNamespace));
	}
	
	public TabularShapeFactory(ShapeManager shapeManager, String tabularPropertyNamespace, TabularLinkingStrategy linkingStrategy) {
		this.shapeManager = shapeManager;
		this.tabularPropertyNamespace = tabularPropertyNamespace;
		this.linkingStrategy = linkingStrategy;
	}

	public void processAll(Collection<Shape> shapeList) throws TabularShapeException {
		for (Shape shape : shapeList) {
			process(shape);
		}
	}

	public List<Shape> process(Shape shape) throws TabularShapeException {
		List<Shape> result = null;

		if (shape.getTabularOriginShape() != null && (shape.getProperty() == null || shape.getProperty().isEmpty())) {

			FormulaBuilder formula = new FormulaBuilder();
			result = new ArrayList<>();
			Shape origin = shape.getTabularOriginShape();
			shape.setNodeKind(origin.getNodeKind());
			if (shape.getTargetClass() == null) {
				if (origin.getTargetClass() == null) {
					throw new TabularShapeException(
							"targetClass of " + origin.getIri().getLocalName() + "> must be defined");
				}
				shape.setTargetClass(origin.getTargetClass());
			} else if (!shape.getTargetClass().equals(origin.getTargetClass())) {
				String msg = MessageFormat.format(
						"The targetClass of Shape <{0}> should be <{1}> as defined in <{2}>, but was <{3}>",
						shape.getIri().getLocalName(), origin.getTargetClass().getLocalName(),
						origin.getIri().getLocalName(), shape.getTargetClass().getLocalName());
				throw new TabularShapeException(msg);
			}
			linkingStrategy.addPrimaryKey(shape);
			addProperties(shape, origin.getProperty(), "", formula, result);

		}

		return result;
	}
	


	
	private void addProperties(Shape sink, List<PropertyConstraint> propertyList, String fieldPrefix, FormulaBuilder formula, List<Shape> result) throws TabularShapeException {

		for (PropertyConstraint originProperty : propertyList) {
			
			PropertyType propertyType = propertyType(originProperty);
			
			switch (propertyType) {
			case OneLiteral :
				oneLiteral(sink, originProperty, fieldPrefix, formula);
				break;
				
			case ManyLiterals :
				manyLiterals(sink, originProperty, fieldPrefix, result);
				break;
				
			case OneObjectValue :
				oneObjectValue(sink, originProperty, fieldPrefix, formula, result);
				break;
				
			case ManyObjectValues :
				manyObjectValues(sink, originProperty, fieldPrefix, result);
				break;
				
			case ManyObjectReferences :
				manyObjectReferences(sink, originProperty, fieldPrefix, formula, result);
				break;
				
			case OneObjectReference:
				oneObjectReference(sink, originProperty, fieldPrefix, formula, result);
				break;
			}
			
		}
	}


	private void manyObjectReferences(
		Shape tabularShape, 
		PropertyConstraint originProperty, 
		String fieldPrefix,
		FormulaBuilder formula,
		List<Shape> result
	) throws TabularShapeException {

		URI objectAlias = snakeCase(fieldPrefix, originProperty.getPredicate());
		Shape shape = reificationShape(tabularShape, originProperty, objectAlias, result);
		
		linkingStrategy.addSingleIriReference(shape, originProperty);
	
		
	}

	private void oneObjectValue(
		Shape sink, 
		PropertyConstraint originProperty,
		String fieldPrefix,
		FormulaBuilder formula, 
		List<Shape> result
	) throws TabularShapeException {
		
		formula.out(originProperty.getPredicate());
		
		Shape valueShape = originProperty.getShape();
		fieldPrefix = fieldPrefix + StringUtil.LABEL_TO_SNAKE_CASE(originProperty.getPredicate().getLocalName()) + "__";
		addProperties(sink, valueShape.getProperty(), fieldPrefix, formula, result);
		
		formula.pop();
	}


	private void manyObjectValues(Shape tabularShape, PropertyConstraint originProperty, String fieldPrefix, List<Shape> result) throws TabularShapeException {

		linkingStrategy.addPrimaryKey(tabularShape);
		
		URI objectAlias = snakeCase(fieldPrefix, originProperty.getPredicate());
		Shape shape = reificationShape(tabularShape, originProperty, objectAlias, result);
		
		FormulaBuilder formula = new FormulaBuilder().out(RDF.SUBJECT).out(originProperty.getPredicate());
		addProperties(shape, originProperty.getShape().getProperty(), "", formula, result);

		
	}

	private Shape reificationShape(Shape tabularShape, PropertyConstraint originProperty, URI objectAlias, List<Shape> result) throws TabularShapeException {

		URI reificationShapeId = namer.reifiedPropertyShapeId(tabularShape.getIri(), originProperty.getPredicate());
		
		Shape shape = new Shape(reificationShapeId);
		shape.setTargetClass(RDF.STATEMENT);
		
		PropertyConstraint subject = new PropertyConstraint(RDF.SUBJECT);
		subject.setMinCount(1);
		subject.setMaxCount(1);
		subject.setValueClass(tabularShape.getTargetClass());
		shape.addDerivedProperty(subject);
		
		PropertyConstraint predicate = new PropertyConstraint(RDF.PREDICATE);
		predicate.setMinCount(1);
		predicate.setMaxCount(1);
		predicate.setIn(valueList(originProperty.getPredicate()));
		shape.addDerivedProperty(predicate);
		
		PropertyConstraint object = new PropertyConstraint(RDF.OBJECT);
		object.setMinCount(1);
		object.setMaxCount(1);
		shape.addDerivedProperty(object);

		if (tabularShape.getShapeDataSource() != null) {
			for (DataSource ds : tabularShape.getShapeDataSource()) {
				if (ds instanceof TableDataSource) {
					TableDataSource tds = (TableDataSource) ds;
					
					TableDataSource table = tds.generateAssociationTable(tabularShape, originProperty.getPredicate());
					shape.addShapeDataSource(table);
				}
			}
		}
		

		linkingStrategy.addAssociationSubject(shape, originProperty);
		
		result.add(shape);
		shapeManager.addShape(shape);
		
		return shape;
	}

	private void manyLiterals(Shape tabularShape, PropertyConstraint originProperty, String fieldPrefix, List<Shape> result) throws TabularShapeException {
		

		URI objectAlias = snakeCase(fieldPrefix, originProperty.getPredicate());
		Shape shape = reificationShape(tabularShape, originProperty, objectAlias, result);
		
		
		PropertyConstraint objectConstraint = originProperty.deepClone();
		objectConstraint.setPredicate(objectAlias);
		objectConstraint.setMinCount(1);
		objectConstraint.setMaxCount(1);
	
		shape.add(objectConstraint);
		
		
	}

	private PropertyConstraint foreignKey(Shape tabularShape) throws TabularShapeException {
		
		URI targetClass = tabularShape.getTargetClass();
		
		if (tabularShape.getNodeKind() == NodeKind.IRI) {
			String className = StringUtil.SNAKE_CASE(targetClass.getLocalName());
			StringBuilder builder = new StringBuilder();
			builder.append(tabularPropertyNamespace);
			builder.append(className);
			builder.append("_ID");
			
			URI predicate = uri(builder.toString());
			
			PropertyConstraint p = new PropertyConstraint(predicate);
			p.setMinCount(1);
			p.setMaxCount(1);
			p.setValueClass(targetClass);
			p.setNodeKind(NodeKind.IRI);
			

			FormulaBuilder formulaBuilder = new FormulaBuilder();
			p.setFormula(formulaBuilder.out(RDF.SUBJECT).build());
			
			return p;
			
		}
		
		throw new TabularShapeException("Unable to create foreign key");
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

	private List<Value> valueList(URI predicate) {
		List<Value> result = new ArrayList<>();
		result.add(predicate);
		return result;
	}

	private PropertyType propertyType(PropertyConstraint originProperty) throws TabularShapeException {

		Integer maxCount = originProperty.getMaxCount();
		if (maxCount != null && maxCount.intValue()==1) {
			// At most one value
			
			if (originProperty.getDatatype() != null) {
				return PropertyType.OneLiteral;
			}
			if (originProperty.getShape() != null) {
				return PropertyType.OneObjectValue;
			}
			if (originProperty.getValueClass() !=null && originProperty.getNodeKind()==NodeKind.IRI) {
				return PropertyType.OneObjectReference;
			}
			
		} else {
			// Multi-value
			
			if (originProperty.getDatatype() != null) {
				return PropertyType.ManyLiterals;
			}
			if (originProperty.getShape() != null) {
				return PropertyType.ManyObjectValues;
			}
			if (originProperty.getValueClass() !=null && originProperty.getNodeKind()==NodeKind.IRI) {
				return PropertyType.ManyObjectReferences;
			}
			
		}
		throw new TabularShapeException("Unsupported PropertyType");
	}

	private void oneObjectReference(Shape sink, PropertyConstraint originProperty, String fieldPrefix,
			FormulaBuilder formula, List<Shape> result) throws TabularShapeException {
		
		linkingStrategy.addSingleIriReference(sink, originProperty);
		
	}

	private void oneLiteral(Shape shape, PropertyConstraint p, String fieldPrefix, FormulaBuilder formula) throws TabularShapeException {
		PropertyConstraint clone = p.deepClone();
		
		
		URI newPredicate = snakeCase(fieldPrefix, p.getPredicate());
		clone.setPredicate(newPredicate);
		clone.setFormula(formula.out(p.getPredicate()).build());
		formula.pop();
		shape.add(clone);
	}
	
	
	private URI snakeCase(String fieldPrefix, URI predicate) throws TabularShapeException {
		if (tabularPropertyNamespace == null) {
			throw new TabularShapeException("tabularPropertyNamespace must be defined");
		}
		
		String newLocalName = StringUtil.SNAKE_CASE(predicate.getLocalName());
		return new URIImpl(tabularPropertyNamespace + fieldPrefix + newLocalName);
	}
	

}

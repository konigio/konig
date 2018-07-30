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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class RdbmsShapeGeneratorTest extends AbstractRdbmsShapeGeneratorTest {

	@Test
	public void testFlatten() throws Exception {
		load("src/test/resources/nested-entity");

		URI shapeId = iri("http://example.com/shapes/PersonRdbmsShape");

		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);
		assertTrue(rdbmsShape != null);

		assertTrue(rdbmsShape.getPropertyConstraint(Schema.givenName) == null);

		URI GIVEN_NAME = iri(ALIAS + "ADDRESS__STREET_ADDRESS");
		PropertyConstraint p = rdbmsShape.getPropertyConstraint(GIVEN_NAME);
		assertTrue(p != null);

		QuantifiedExpression formula = p.getFormula();
		assertTrue(formula != null);

		String text = formula.getText();
		assertEquals(".address.streetAddress", text);
	}

	/**
	 * Case 1 : If the parent logical shape does not have a property with
	 * stereotype equal to konig:primaryKey or konig:syntheticKey and the parent
	 * logical shape does not have sh:nodeKind equal to sh:IRI, then the parent
	 * RDBMS shape must introduce a synthetic key property as described below.
	 * The local name of this property shall be the SNAKE_CASE version of the
	 * OWL Class with __PK appended as a suffix. In the example above, the local
	 * name would be PRODUCT__PK. The stereotype of this property shall be
	 * konig:syntheticKey. The sh:datatype of this property shall be xsd:long.
	 * The sh:minCount of this property shall be 1. The sh:maxCount of this
	 * property shall be 1.
	 * 
	 */
	@Test
	public void testOneToManyCase1() throws Exception {
		load("src/test/resources/one-many-relation");

		AwsShapeConfig.init();
		URI shapeId = iri("https://schema.pearson.com/shapes/ProductRdbmsShape");

		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);

		assertTrue(rdbmsShape != null);
		assertTrue(rdbmsShape.getNodeKind() == null);
		assertTrue(hasPrimaryKey(logicalShape) == null);

		URI PRODUCT_PK = iri(ALIAS + "PRODUCT_PK");
		PropertyConstraint p = rdbmsShape.getPropertyConstraint(PRODUCT_PK);
		assertTrue(p != null);
		assertTrue(p.getStereotype() == Konig.syntheticKey);
		assertTrue(p.getDatatype() == XMLSchema.LONG);
		assertTrue(p.getMinCount() == 1);
		assertTrue(p.getMaxCount() == 1);
		assertTrue(p.getFormula() == null);

		Shape rdbmsChildShape = null;
		Shape childShape = null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			if (pc.getShape() != null) {
				childShape = getRdbmsShapeFromLogicalShape(pc.getShape());
				rdbmsChildShape = shapeGenerator.createOneToManyChildShape(logicalShape, pc.getPredicate(),
						childShape);
				break;
			}
		}

		URI PRODUCT_FK = iri(ALIAS + "PRODUCT_FK");
		PropertyConstraint p1 = rdbmsChildShape.getPropertyConstraint(PRODUCT_FK);
		assertTrue(p1 != null);

		QuantifiedExpression formula = p1.getFormula();
		assertTrue(formula != null);

		String text = formula.getText();
		assertEquals("^contributor.PRODUCT_PK", text);

		PropertyConstraint p2 = rdbmsChildShape.getPropertyConstraint(iri("http://www.konig.io/ns/core/ID"));
		assertTrue(p2 != null);

		for (PropertyConstraint pc : childShape.getTabularOriginShape().getProperty()) {
			if (pc.getShape() != null) {
				rdbmsChildShape = shapeGenerator.createOneToManyChildShape(childShape, pc.getPredicate(),
						getRdbmsShapeFromLogicalShape(pc.getShape()));
				break;
			}
		}

		PropertyConstraint p3 = rdbmsChildShape.getPropertyConstraint(iri("http://www.konig.io/ns/core/ID"));
		assertTrue(p3 != null);

		QuantifiedExpression formula1 = p3.getFormula();
		assertTrue(formula1 != null);

		assertEquals("^person", formula1.getText());
	}
	public Shape getRdbmsShapeFromLogicalShape(Shape childShape) {
		for(Shape shape:shapeManager.listShapes()){
			if(shape.getTabularOriginShape()!=null && shape.getTabularOriginShape().getId().equals(childShape.getId())){
				return shape;
			}
		}
		return null;
	}
	/***
	 * Case 2 : If the predicate that describes the one-to-many relationship
	 * does not have exactly one inverse, then the following rules apply: If the
	 * child shape has no other relationship to an object of the same type as
	 * the parent object, then the name of the foreign key property on the child
	 * RDBMS shape shall be given by the SNAKE_CASE version of the local name
	 * for the OWL class of the parent object with __FK added as a suffix. In
	 * the example above, the parent object is an instance of the OWL class
	 * mdm:Product. Thus the foreign key property would be named PRODUCT__FK.
	 * 
	 * Case 3 : The property constraint for the foreign key shall include a
	 * konig:formula property that gives the path to the value from the parent
	 * object that is stored in the foreign key property. The formula shall be
	 * determined according to the following rules: the path must point to the
	 * primary key or synthetic key defined on the parent object. For example:
	 * ^contributor.PRODUCT__PK.
	 */
	@Test
	public void testOneToManyCase2() throws Exception {
		load("src/test/resources/one-many-relation");

		AwsShapeConfig.init();
		URI shapeId = iri("https://schema.pearson.com/shapes/ProductRdbmsShape");

		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);
		Shape rdbmsChildShape = null;
		Shape childShape = null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			if (pc.getShape() != null) {
				childShape = getRdbmsShapeFromLogicalShape(pc.getShape());
				rdbmsChildShape = shapeGenerator.createOneToManyChildShape(logicalShape, pc.getPredicate(),
						childShape);
				break;
			}
		}

		URI PRODUCT_FK = iri(ALIAS + "PRODUCT_FK");
		PropertyConstraint p1 = rdbmsChildShape.getPropertyConstraint(PRODUCT_FK);
		assertTrue(p1 != null);

		QuantifiedExpression formula = p1.getFormula();
		assertTrue(formula != null);

		String text = formula.getText();
		assertEquals("^contributor.PRODUCT_PK", text);

		PropertyConstraint p2 = rdbmsChildShape.getPropertyConstraint(iri("http://www.konig.io/ns/core/ID"));
		assertTrue(p2 != null);

		for (PropertyConstraint pc : childShape.getTabularOriginShape().getProperty()) {
			if (pc.getShape() != null) {
				rdbmsChildShape = shapeGenerator.createOneToManyChildShape(childShape, pc.getPredicate(),
						getRdbmsShapeFromLogicalShape(pc.getShape()));
				break;
			}
		}

		PropertyConstraint p3 = rdbmsChildShape.getPropertyConstraint(iri("http://www.konig.io/ns/core/ID"));
		assertTrue(p3 != null);

		QuantifiedExpression formula1 = p3.getFormula();
		assertTrue(formula1 != null);

		assertEquals("^person", formula1.getText());
	}

	/***
	 * Case 4 : If the predicate that describes the one-to-many relationship has
	 * exactly one inverse property (specified via owl:inverseOf), then the name
	 * of the foreign key property in the child RDBMS shape shall be the
	 * SNAKE_CASE version of the inverse property's local name with __FK
	 * appended as a suffix. For instance, in the example above, suppose that
	 * mdm:contributesTo is the inverse of mdm:contributor. Then the foreign key
	 * property on the RDBMS Shape for the child object would be named
	 * CONTRIBUTES_TO__FK.
	 * 
	 */
	@Test
	public void testOneToManyCase4() throws Exception {
		load("src/test/resources/one-many-relation");

		AwsShapeConfig.init();
		URI shapeId = iri("https://schema.pearson.com/shapes/ProductRdbmsShape");

		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);
		OwlReasoner reasoner = new OwlReasoner(graph);
		shapeGenerator = new RdbmsShapeGenerator(null, reasoner,shapeManager);

		Shape rdbmsChildShape = null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			if (pc.getShape() != null) {
				rdbmsChildShape = shapeGenerator.createOneToManyChildShape(logicalShape, pc.getPredicate(),
						getRdbmsShapeFromLogicalShape(pc.getShape()));
				break;
			}
		}

		URI CONTRIBUTES_TO_FK = iri(ALIAS + "CONTRIBUTES_TO_FK");
		PropertyConstraint p1 = rdbmsChildShape.getPropertyConstraint(CONTRIBUTES_TO_FK);
		assertTrue(p1 != null);

		QuantifiedExpression formula = p1.getFormula();
		assertTrue(formula != null);

		String text = formula.getText();
		assertEquals(".contributesTo", text);

		PropertyConstraint p2 = rdbmsChildShape.getPropertyConstraint(iri("http://www.konig.io/ns/core/ID"));
		assertTrue(p2 != null);
	}

	/**
	 * Case 3 : The property constraint for the foreign key shall include a
	 * konig:formula property that gives the path to the value from the parent
	 * object that is stored in the foreign key property. The formula shall be
	 * determined according to the following rules: If the logical Shape of the
	 * parent object has sh:nodeKind equal to sh:IRI, then the formula is just
	 * the inverse of the forward property. In the example above, the formula
	 * would be ^contributor
	 */

	@Test
	public void testOneToManyCase3() throws Exception {
		load("src/test/resources/one-many-relation");

		AwsShapeConfig.init();
		URI shapeId = iri("https://schema.pearson.com/shapes/ProductRdbmsShape");

		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);
		Shape rdbmsChildShape = null;
		Shape childShape = null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			if (pc.getShape() != null) {
				childShape = getRdbmsShapeFromLogicalShape(pc.getShape());
				rdbmsChildShape = shapeGenerator.createOneToManyChildShape(logicalShape, pc.getPredicate(),
						childShape);
				break;
			}
		}

		for (PropertyConstraint pc : childShape.getTabularOriginShape().getProperty()) {
			if (pc.getShape() != null) {
				rdbmsChildShape = shapeGenerator.createOneToManyChildShape(childShape, pc.getPredicate(),
						getRdbmsShapeFromLogicalShape(pc.getShape()));
				break;
			}
		}

		PropertyConstraint p3 = rdbmsChildShape.getPropertyConstraint(iri("http://www.konig.io/ns/core/ID"));
		assertTrue(p3 != null);

		QuantifiedExpression formula1 = p3.getFormula();
		assertTrue(formula1 != null);

		assertEquals("^person", formula1.getText());
	}
	/**
	 * Case 1 : Both the shapes having many to many relationship have sh:nodeKind as sh:IRI
	 * and hence the association shape created will have 
	 * sh:targetClass as the parent class which have the konig:ManyToMany relationship in the property constraint 
	 * and sh:nodeKind as sh:IRI. The association shape will also include another property that refers to the id property 
	 * of the other shape. The sh:class should be set and the formula will be set as '.' relationshipProperty. 
	 */
	@Test
	public void testManyToManyCase1() throws Exception{
		load("src/test/resources/many-to-many-relation-id");

		AwsShapeConfig.init();
		URI shapeId = iri("https://schema.pearson.com/shapes/ProductRdbmsShape");

		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);
		List<Shape> manyToManyShapes = null;
		Shape childRdbmsShape = null;
		Shape childShape = null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			childShape=pc.getShape();
			if (childShape != null) {
				childRdbmsShape = getRdbmsShapeFromLogicalShape(childShape);
				manyToManyShapes = shapeGenerator.createManyToManyChildShape(logicalShape, pc,
						childRdbmsShape);
				break;
			}
		}
		assertTrue(rdbmsShape!=null);
		assertTrue(rdbmsShape.getPropertyConstraint(iri("http://www.konig.io/ns/core/ID"))!=null);
		assertTrue(manyToManyShapes!=null && manyToManyShapes.size()==2);
		assertTrue(manyToManyShapes.get(1).getPropertyConstraint(iri("http://www.konig.io/ns/core/ID"))!=null);
		
		assertTrue(manyToManyShapes.get(0).getTargetClass().equals(logicalShape.getTabularOriginShape().getTargetClass()));
		PropertyConstraint childShapeRef=manyToManyShapes.get(0).getPropertyConstraint(iri("http://example.com/ns/alias/CONTRIBUTOR"));
		assertTrue(childShapeRef!=null);
		assertTrue(((URI)childShapeRef.getValueClass()).equals(childShape.getTargetClass()));
		assertEquals(".contributor", childShapeRef.getFormula().getText());
		
	}
	/**
	 * Case 2 : Both the shapes having many to many relationship have konig:stereotype as konig:primaryKey or
	 * konig:syntheticKey and hence the association shape created will have the foreign key reference properties with suffix "_FK"
	 * and the target class as the parent class which has konig:ManyToMany relationship in the property constraint 
	 * and the formula for the parent shape reference will be the primary key property and the formula for the child shape reference 
	 * will be the "." relationshipProperty "."  primary key property.
	 */
	@Test
	public void testManyToManyCase2() throws Exception{
		load("src/test/resources/many-to-many-relation-pk");

		AwsShapeConfig.init();
		URI shapeId = iri("https://schema.pearson.com/shapes/ProductRdbmsShape");

		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);
		List<Shape> manyToManyShapes = null;
		Shape childRdbmsShape = null;
		Shape childShape = null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			childShape=pc.getShape();
			if (childShape != null) {
				childRdbmsShape = getRdbmsShapeFromLogicalShape(childShape);
				manyToManyShapes = shapeGenerator.createManyToManyChildShape(logicalShape, pc,
						childRdbmsShape);
				break;
			}
		}
		assertTrue(rdbmsShape!=null);
		assertTrue(rdbmsShape.getPropertyConstraint(iri("http://example.com/ns/alias/PPID_PK"))!=null);
		assertTrue(manyToManyShapes!=null && manyToManyShapes.size()==2);
		assertTrue(manyToManyShapes.get(1).getPropertyConstraint(iri("http://example.com/ns/alias/CONTIBUTOR_ID_PK"))!=null);
		
		PropertyConstraint childShapeRef=manyToManyShapes.get(0).getPropertyConstraint(iri("http://example.com/ns/alias/CONTRIBUTOR"));
		assertTrue(childShapeRef!=null);
		assertTrue(((URI)childShapeRef.getValueClass()).equals(childShape.getTargetClass()));
		assertEquals(".contributor", childShapeRef.getFormula().getText());
		
	}
	
	/**
	 * Case 3 : Both the shapes having many to many relationship do not have konig:stereotype as konig:primaryKey or
	 * konig:syntheticKey and hence the association shape created will have the foreign key reference properties with suffix "_FK"
	 * and the target class as the parent class which has konig:ManyToMany relationship in the property constraint 
	 * and the formula for the parent shape reference will be the primary key property and the formula for the child shape reference 
	 * will be the "." relationshipProperty "."  primary key property.
	 */
	@Test
	public void testManyToManyCase3() throws Exception{
		load("src/test/resources/many-to-many-relation-create-pk");

		AwsShapeConfig.init();
		URI shapeId = iri("https://schema.pearson.com/shapes/ProductRdbmsShape");

		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);
		List<Shape> manyToManyShapes = null;
		Shape childRdbmsShape = null;
		Shape childShape = null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			childShape=pc.getShape();
			if (childShape != null) {
				childRdbmsShape = getRdbmsShapeFromLogicalShape(childShape);
				manyToManyShapes = shapeGenerator.createManyToManyChildShape(logicalShape, pc,
						childRdbmsShape);
				break;
			}
		}
		assertTrue(rdbmsShape!=null);
		assertTrue(rdbmsShape.getPropertyConstraint(iri("http://example.com/ns/alias/PRODUCT_PK"))!=null);
		assertTrue(manyToManyShapes!=null && manyToManyShapes.size()==2);
		assertTrue(manyToManyShapes.get(1).getPropertyConstraint(iri("http://example.com/ns/alias/PRODUCT_CONTRIBUTOR_PK"))!=null);
		
		PropertyConstraint childShapeRef=manyToManyShapes.get(0).getPropertyConstraint(iri("http://example.com/ns/alias/PRODUCT_CONTRIBUTOR_FK"));
		assertTrue(childShapeRef!=null);
		assertEquals(".contributor.PRODUCT_CONTRIBUTOR_PK", childShapeRef.getFormula().getText());
		
	}
	
	private PropertyConstraint hasPrimaryKey(Shape rdbmsShape) {
		for (PropertyConstraint p : rdbmsShape.getProperty()) {
			if (p.getStereotype() != null
					&& (p.getStereotype().equals(Konig.syntheticKey) || p.getStereotype().equals(Konig.primaryKey))) {
				return p;
			}
		}

		return null;
	}

}

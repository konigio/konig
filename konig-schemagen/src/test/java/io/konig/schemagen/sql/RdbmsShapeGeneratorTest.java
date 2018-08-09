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

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PredicatePath;
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
		PropertyConstraint relationshipPc=null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			childShape=pc.getShape();
			if (childShape != null) {
				relationshipPc=pc;
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
		
		Shape assocShape=manyToManyShapes.get(0);
		assertTrue(assocShape.getType().contains(Konig.AssociationShape) && assocShape.getType().contains(Konig.TabularNodeShape));
		assertTrue(assocShape.getDerivedProperty()!=null && assocShape.getDerivedProperty().size()==1);
		PropertyConstraint derivedPc=assocShape.getDerivedProperty().get(0);
		assertTrue(derivedPc.getPath() instanceof PredicatePath && RDF.PREDICATE.equals(((PredicatePath)derivedPc.getPath()).getPredicate()));
		assertTrue(derivedPc.getHasValue().contains(relationshipPc.getPredicate()));
		List<PropertyConstraint> pcList=assocShape.getProperty();
		assertTrue("subject".equals(pcList.get(0).getFormula().getText()));
		assertTrue(assocShape.getPropertyConstraint(iri("http://example.com/ns/alias/PRODUCT_ID"))!=null);
		assertTrue("object".equals(pcList.get(1).getFormula().getText()));
		assertTrue(assocShape.getPropertyConstraint(iri("http://example.com/ns/alias/PRODUCTCONTRIBUTOR_ID"))!=null);
	}
	
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
		PropertyConstraint relationshipPc =null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			childShape=pc.getShape();
			if (childShape != null) {
				relationshipPc=pc;
				childRdbmsShape = getRdbmsShapeFromLogicalShape(childShape);
				manyToManyShapes = shapeGenerator.createManyToManyChildShape(logicalShape, pc,
						childRdbmsShape);
				break;
			}
		}
		assertTrue(rdbmsShape!=null);
		assertTrue(rdbmsShape.getPropertyConstraint(iri("http://example.com/ns/alias/PPID_PK"))!=null);
		assertTrue(manyToManyShapes!=null && manyToManyShapes.size()==2);
		assertTrue(manyToManyShapes.get(1).getPropertyConstraint(iri("http://example.com/ns/alias/CONTRIBUTOR_ID_PK"))!=null);
		
		PropertyConstraint parentShapeRef=manyToManyShapes.get(0).getPropertyConstraint(iri("http://example.com/ns/alias/PRODUCT_FK"));
		assertTrue(parentShapeRef!=null);		
		assertEquals("subject.PPID_PK", parentShapeRef.getFormula().getText());
		
		PropertyConstraint childShapeRef=manyToManyShapes.get(0).getPropertyConstraint(iri("http://example.com/ns/alias/PRODUCT_CONTRIBUTOR_FK"));
		assertTrue(childShapeRef!=null);
		assertEquals("object.CONTRIBUTOR_ID_PK", childShapeRef.getFormula().getText());
		
		Shape assocShape=manyToManyShapes.get(0);
		assertTrue(assocShape.getType().contains(Konig.AssociationShape) && assocShape.getType().contains(Konig.TabularNodeShape));
		assertTrue(assocShape.getDerivedProperty()!=null && assocShape.getDerivedProperty().size()==1);
		PropertyConstraint derivedPc=assocShape.getDerivedProperty().get(0);
		assertTrue(derivedPc.getPath() instanceof PredicatePath && RDF.PREDICATE.equals(((PredicatePath)derivedPc.getPath()).getPredicate()));
		assertTrue(derivedPc.getHasValue().contains(relationshipPc.getPredicate()));
		
		
	}
	
	@Test
	public void testManyToManyAssocShapeExists() throws Exception{
		load("src/test/resources/many-to-many-relation-assoc-shape");

		AwsShapeConfig.init();
		URI shapeId = iri("https://schema.pearson.com/shapes/ProductRdbmsShape");

		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);
		List<Shape> manyToManyShapes = null;
		Shape childRdbmsShape = null;
		Shape childShape = null;
		PropertyConstraint relationshipPc =null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			childShape=pc.getShape();
			if (childShape != null) {
				relationshipPc=pc;
				childRdbmsShape = getRdbmsShapeFromLogicalShape(childShape);
				manyToManyShapes = shapeGenerator.createManyToManyChildShape(logicalShape, pc,
						childRdbmsShape);
				break;
			}
		}
		assertTrue(rdbmsShape!=null);
		assertTrue(rdbmsShape.getPropertyConstraint(iri("http://example.com/ns/alias/PPID_PK"))!=null);
		assertTrue(manyToManyShapes!=null && manyToManyShapes.size()==1);
		//Association shape already exists and hence it will not create an association shape.
		assertTrue(manyToManyShapes.get(0).getPropertyConstraint(iri("http://example.com/ns/alias/CONTRIBUTOR_ID_PK"))!=null);
		
	}
	
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
		PropertyConstraint relationshipPc=null;
		for (PropertyConstraint pc : logicalShape.getTabularOriginShape().getProperty()) {
			childShape=pc.getShape();
			if (childShape != null) {
				relationshipPc=pc;
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
		
		PropertyConstraint parentShapeRef=manyToManyShapes.get(0).getPropertyConstraint(iri("http://example.com/ns/alias/PRODUCT_FK"));
		assertTrue(parentShapeRef!=null);		
		assertEquals("subject.PRODUCT_PK", parentShapeRef.getFormula().getText());
		
		PropertyConstraint childShapeRef=manyToManyShapes.get(0).getPropertyConstraint(iri("http://example.com/ns/alias/PRODUCT_CONTRIBUTOR_FK"));
		assertTrue(childShapeRef!=null);
		assertEquals("object.PRODUCT_CONTRIBUTOR_PK", childShapeRef.getFormula().getText());
		
		Shape assocShape=manyToManyShapes.get(0);
		assertTrue(assocShape.getType().contains(Konig.AssociationShape) && assocShape.getType().contains(Konig.TabularNodeShape));
		assertTrue(assocShape.getDerivedProperty()!=null && assocShape.getDerivedProperty().size()==1);
		PropertyConstraint derivedPc=assocShape.getDerivedProperty().get(0);
		assertTrue(derivedPc.getPath() instanceof PredicatePath && RDF.PREDICATE.equals(((PredicatePath)derivedPc.getPath()).getPredicate()));
		assertTrue(derivedPc.getHasValue().contains(relationshipPc.getPredicate()));
		
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

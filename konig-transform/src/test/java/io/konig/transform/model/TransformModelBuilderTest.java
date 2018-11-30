package io.konig.transform.model;

/*
 * #%L
 * Konig Transform
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class TransformModelBuilderTest {

	protected MemoryGraph graph = new MemoryGraph(MemoryNamespaceManager.getDefaultInstance());
	protected ShapeManager shapeManager = new MemoryShapeManager();
	protected OwlReasoner owlReasoner = new OwlReasoner(graph);
	protected TransformModelBuilder builder = new TransformModelBuilder(shapeManager, owlReasoner);
	protected ShapeBuilder shapeBuilder = new ShapeBuilder(shapeManager);
	
	@Test
	public void testIriTemplate() throws Exception {
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		URI personId = uri("http://example.com/ns/alias/person_id");
		
		shapeBuilder
			.beginShape(sourceShapeId)
				.targetClass(Schema.Person)
				.iriTemplate("http://example.com/resources/Person/{person_id}")
				.beginProperty(personId)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/Person"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("Person")
				.endDataSource()
			.endShape()
			.beginShape(targetShapeId)
				.targetClass(Schema.Person)
				.nodeKind(NodeKind.IRI)
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/Person"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("Person_STG")
				.endDataSource()
			.endShape();
		
		Shape shape = shapeManager.getShapeById(targetShapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		
		TNodeShape rootShape = builder.build(shape, ds);
	
		TPropertyShape idProperty = rootShape.getProperty(Konig.id);
		assertTrue(idProperty != null);
		
	}
	
	@Ignore
	public void testConcat() throws Exception {
		URI shapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		URI personId = uri("http://example.com/ns/PERSON_ID");
		shapeBuilder
			.beginShape(shapeId)
				.targetClass(Schema.Person)
				.beginProperty(Konig.uid)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/Person"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("Person")
				.endDataSource()
			.endShape()
			.beginShape(sourceShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginProperty(personId)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				.beginDerivedProperty(Konig.uid)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/SourcePerson"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("SourcePerson")
				.endDataSource()
			.endShape()
			;
		
		Shape shape = shapeManager.getShapeById(shapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		ShapeTransformException e = null;
		try {
			builder.build(shape, ds); 
		} catch (ShapeTransformException oops) {
			e = oops;
		}
		
		assertTrue(e != null);
	}
	
	@Ignore
	public void testUnmatchedProperty() throws Exception {
		URI shapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		shapeBuilder
			.beginShape(shapeId)
				.targetClass(Schema.Person)
				.beginProperty(Konig.uid)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/Person"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("Person")
				.endDataSource()
			.endShape()
			.beginShape(sourceShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/SourcePerson"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("SourcePerson")
				.endDataSource()
			.endShape()
			;
		
		Shape shape = shapeManager.getShapeById(shapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		ShapeTransformException e = null;
		try {
			builder.build(shape, ds); 
		} catch (ShapeTransformException oops) {
			e = oops;
		}
		
		assertTrue(e != null);
	}
	
	@Ignore
	public void testExactMatchDatatypeProperty() throws Exception {
		
		URI shapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		shapeBuilder
			.beginShape(shapeId)
				.targetClass(Schema.Person)
				.beginProperty(Konig.uid)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/Person"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("Person")
				.endDataSource()
			.endShape()
			.beginShape(sourceShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginProperty(Konig.uid)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/SourcePerson"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("SourcePerson")
				.endDataSource()
			.endShape()
			;
		
		Shape shape = shapeManager.getShapeById(shapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		
		TNodeShape rootShape = builder.build(shape, ds);
		
		TPropertyShape givenName = rootShape.getProperty(Schema.givenName);
		assertTrue(givenName != null);
		
		TProperty givenNameGroup = givenName.getPropertyGroup();
		assertTrue(givenNameGroup != null);
		
		TClass rootClass = rootShape.getTclass();
		assertTrue(rootClass != null);
		assertEquals(Schema.Person, rootClass.getId());
		
		Set<TNodeShape> sourceShapes = rootClass.getSourceShapes();
		assertEquals(1, sourceShapes.size());
		
		TNodeShape sourceShape = sourceShapes.iterator().next();
		assertEquals(sourceShape.getShape().getId(), sourceShapeId);
		
		TExpression value = givenName.getPropertyGroup().getValueExpression();
		assertTrue(value instanceof ValueOfExpression);
		ValueOfExpression valueOf = (ValueOfExpression) value;
		assertEquals(Schema.givenName, valueOf.getTpropertyShape().getPredicate());
		
		rootShape.getFromClause();
		
		List<TFromItem> fromClause = rootShape.getFromClause();
		assertEquals(1, fromClause.size());
		TFromItem fromItem = fromClause.get(0);
		assertEquals(sourceShape, fromItem.getSourceShape());
		
		
	}
	
	@Ignore
	public void testRenameDatatypeProperty() throws Exception {
		
		URI shapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		URI firstName = uri("http://example.com/ns/alias/first_name");
		shapeBuilder
			.beginShape(shapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/Person"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("Person")
				.endDataSource()
			.endShape()
			.beginShape(sourceShapeId)
				.targetClass(Schema.Person)
				.beginProperty(firstName)
					.beginFormula()
						.beginPath()
							.out(Schema.givenName)
						.endPath()
					.endFormula()
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/SourcePerson"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("SourcePerson")
				.endDataSource()
			.endShape()
			;
		
		Shape shape = shapeManager.getShapeById(shapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		
		TNodeShape rootShape = builder.build(shape, ds);
		
		TPropertyShape givenName = rootShape.getProperty(Schema.givenName);
		assertTrue(givenName != null);
		
		TProperty givenNameGroup = givenName.getPropertyGroup();
		assertTrue(givenNameGroup != null);
		
		TClass rootClass = rootShape.getTclass();
		assertTrue(rootClass != null);
		assertEquals(Schema.Person, rootClass.getId());
		
		Set<TNodeShape> sourceShapes = rootClass.getSourceShapes();
		assertEquals(1, sourceShapes.size());
		
		TNodeShape sourceShape = sourceShapes.iterator().next();
		assertEquals(sourceShape.getShape().getId(), sourceShapeId);
		
		TExpression value = givenName.getPropertyGroup().getValueExpression();
		assertTrue(value instanceof ValueOfExpression);
		ValueOfExpression valueOf = (ValueOfExpression) value;
		assertEquals(firstName, valueOf.getTpropertyShape().getPredicate());
		
		rootShape.getFromClause();
		
		List<TFromItem> fromClause = rootShape.getFromClause();
		assertEquals(1, fromClause.size());
		TFromItem fromItem = fromClause.get(0);
		assertEquals(sourceShape, fromItem.getSourceShape());
		
	}


	private URI uri(String value) {
		return new URIImpl(value);
	}

}

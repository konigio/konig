package io.konig.validation;

/*
 * #%L
 * Konig Core
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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.shacl.NodeKind;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class ModelValidatorTest {
	
	private ShapeManager shapeManager = new MemoryShapeManager();
	private ModelValidator validator = new ModelValidator();
	private MemoryGraph graph = new MemoryGraph();
	private ModelValidationRequest request;
	
	@Before
	public void setUp() throws Exception {
		OwlReasoner owl = new OwlReasoner(graph);
		request = new ModelValidationRequest(owl, shapeManager);
		CommentConventions comments = new CommentConventions();
		comments.setRequireClassComments(true);
		comments.setRequireNamedIndividualComments(true);
		comments.setRequireNodeShapeComments(true);
		comments.setRequirePropertyComments(true);
		comments.setRequirePropertyShapeComments(true);
		request.setCommentConventions(comments);
	
	}

	@Test
	public void testRequiresMinCount() {
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		new ShapeBuilder(shapeManager)
			.beginShape(uri("http://example.com/shapes/ProductShape"))
			.endShape()
			.beginShape(shapeId)
				.beginProperty(Schema.parent)
				.endProperty()
				.beginProperty(Schema.familyName)
					.minCount(1)
				.endProperty()
			.endShape();
		
		
		ModelValidationReport report = validator.process(request);
		
		List<NodeShapeReport> nodeReports = report.getShapeReports();
		assertEquals(1, nodeReports.size());
		
		PropertyShapeReport familyName = nodeReports.get(0).findPropertyReport(Schema.familyName);
		assertTrue(familyName != null);
		assertTrue(!familyName.isRequiresMinCount());
		
		PropertyShapeReport parent = nodeReports.get(0).findPropertyReport(Schema.parent);
		assertTrue(parent.isRequiresMinCount());
		
		
		
	}
	@Test
	public void testStatistics() {
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		new ShapeBuilder(shapeManager)
			.beginShape(uri("http://example.com/shapes/ProductShape"))
			.endShape()
			.beginShape(shapeId)
				.beginProperty(Schema.parent)
				.endProperty()
				.beginProperty(Schema.familyName)
					.comment("The person's family name (last name in the US)")
				.endProperty()
			.endShape();
		
		edge(Schema.Person, RDF.TYPE, OWL.CLASS);
		edge(Schema.Person, RDFS.COMMENT, literal("A person alive, dead, undead, or fictional"));
		edge(Schema.givenName, RDF.TYPE, OWL.DATATYPEPROPERTY);
		edge(Schema.givenName, RDFS.COMMENT, literal("The person's given name (first name in the US)"));
		edge(Schema.Male, RDF.TYPE, Schema.GenderType);
		edge(Schema.Male, RDFS.COMMENT, literal("The Male gender type"));
		edge(Schema.Female, RDF.TYPE, Schema.GenderType);
		edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		edge(Schema.GenderType, RDFS.COMMENT, literal("The enumeration of allowed values for a person's gender"));

		ModelValidationReport report = validator.process(request);
		
		ModelStatistics stats = report.getStatistics();
		assertEquals(3, stats.getNumberOfClasses());
		assertEquals(3, stats.getNumberOfProperties());
		assertEquals(2, stats.getNumberOfShapes());
		assertEquals(2, stats.getNumberOfNamedIndividuals());
		
		RationalNumber classesWithDescription = report.getStatistics().getClassesWithDescription();
		assertTrue(classesWithDescription != null);
		
		assertEquals(2, classesWithDescription.getNumerator());
		ClassReport enumReport = report.findClassReport(Schema.Enumeration);
		assertTrue(enumReport != null);
		assertTrue(enumReport.getRequiresDescription());
		
		NodeShapeReport personShapeReport = report.findNodeReport(shapeId);
		assertTrue(personShapeReport != null);
		PropertyShapeReport parentReport = personShapeReport.findPropertyReport(Schema.parent);
		assertTrue(parentReport != null);
		assertTrue(parentReport.getRequiresDescription());
		
	
		RationalNumber individualsWithDescription = report.getStatistics().getNamedIndividualsWithDescription();
		assertTrue(individualsWithDescription != null);
		assertEquals(1, individualsWithDescription.getNumerator());
		
		NamedIndividualReport femaleReport = report.findNamedIndividualReport(Schema.Female);
		assertTrue(femaleReport != null);
		assertTrue(femaleReport.getRequiresDescription());
		
		
	}
	
	private Value literal(String value) {
		return new LiteralImpl(value);
	}

	@Test
	public void testRequiresDatatypeClassOrShape() {
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		new ShapeBuilder(shapeManager)
			.beginShape(shapeId)
				.beginProperty(Schema.parent)
				.endProperty()
				
			.endShape();
		

		ModelValidationReport report = validator.process(request);
		
		List<NodeShapeReport> shapeReports = report.getShapeReports();
		assertEquals(1, shapeReports.size());
		NodeShapeReport shapeReport = shapeReports.get(0);
		
		List<PropertyShapeReport> propertyReports = shapeReport.getPropertyReports();
		assertEquals(1, propertyReports.size());
		
		PropertyShapeReport propertyReport = propertyReports.get(0);
		assertTrue(propertyReport.isRequiresDatatypeClassOrShape());
		
	}
	
	@Test
	public void testDatatypeConflictWithRange() {
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		new ShapeBuilder(shapeManager)
			.beginShape(shapeId)
				.beginProperty(Schema.parent)
					.datatype(XMLSchema.STRING)
				.endProperty()
				
			.endShape();
		
		edge(Schema.parent, RDFS.RANGE, Schema.Person);

		ModelValidationReport report = validator.process(request);
		
		List<NodeShapeReport> shapeReports = report.getShapeReports();
		
		assertEquals(1, shapeReports.size());
		NodeShapeReport shapeReport = shapeReports.get(0);
		
		List<PropertyShapeReport> propertyReports = shapeReport.getPropertyReports();
		assertEquals(1, propertyReports.size());
		
		PropertyShapeReport propertyReport = propertyReports.get(0);
		assertEquals(Schema.parent, propertyReport.getPropertyShape().getPredicate());
		
		TypeConflict conflict = propertyReport.getTypeConflict();
		assertTrue(conflict != null);
		assertEquals(XMLSchema.STRING, conflict.getPropertyShapeType());
		assertEquals(Schema.Person, conflict.getRange());
	}
	
	@Test
	public void testDatatype() {

		URI shapeId = uri("http://example.com/shapes/PersonShape");
		new ShapeBuilder(shapeManager)
			.beginShape(shapeId)
				.beginProperty(Schema.givenName)
					.comment("The person's given name")
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				.beginProperty(Schema.familyName)
					.datatype(XMLSchema.STRING)
					.nodeKind(NodeKind.IRI)
				.endProperty()
				.beginProperty(Schema.email)
					.datatype(XMLSchema.STRING)
					.beginValueShape("http://example.com/shapes/FooShape")
					
					.endValueShape()
				.endProperty()
				.beginProperty(Schema.telephone)
					.datatype(XMLSchema.STRING)
					.valueClass(uri("http://example.com/Telephone"))
				.endProperty()
				
			.endShape();

		ModelValidationReport report = validator.process(request);
		
		List<NodeShapeReport> shapeReports = report.getShapeReports();
		assertEquals(1, shapeReports.size());
		
		NodeShapeReport shapeReport = shapeReports.get(0);
		
		PropertyShapeReport givenName = shapeReport.findPropertyReport(Schema.givenName);
		assertTrue(givenName == null);
		

		PropertyShapeReport familyName = shapeReport.findPropertyReport(Schema.familyName);
		assertTrue(familyName != null && familyName.isDatatypeWithIriNodeKind());

		PropertyShapeReport email = shapeReport.findPropertyReport(Schema.email);
		assertTrue(email != null && email.isDatatypeWithShape());

		PropertyShapeReport telephone = shapeReport.findPropertyReport(Schema.telephone);
		assertTrue(telephone != null && telephone.isDatatypeWithClass());
		
	}
	
	@Test
	public void testRequiresShapeOrIriNodeKind() {
		
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		ShapeBuilder builder = new ShapeBuilder(shapeManager);
		
		builder
			.beginShape(shapeId)
				.beginProperty(Schema.parent)
					.valueClass(Schema.Person)
				.endProperty()
				.beginProperty(Schema.children)
					.valueClass(Schema.Person)
					.nodeKind(NodeKind.IRI)
					.comment("A child of the person")
				.endProperty()
			.endShape();
		

		ModelValidationReport report = validator.process(request);
		
		List<NodeShapeReport> shapeReports = report.getShapeReports();
		assertEquals(1, shapeReports.size());
		
		NodeShapeReport shapeReport = shapeReports.get(0);
		
		assertEquals(shapeId, shapeReport.getShapeId());
		
		List<PropertyShapeReport> propertyReports = shapeReport.getPropertyReports();
		assertEquals(1, propertyReports.size());
		
		PropertyShapeReport propertyReport = propertyReports.get(0);
		assertEquals(Schema.parent, propertyReport.getPropertyShape().getPredicate());
		
		assertTrue(propertyReport.isRequiresShapeOrIriNodeKind());
		
	}

	
	@Test
	public void testClassPropertyDisjoint() {
		
		
		edge(Schema.Person, RDF.TYPE, OWL.CLASS);
		edge(Schema.Person, RDF.TYPE, RDF.PROPERTY);
		
		edge(Schema.gender, RDF.TYPE, OWL.CLASS);
		edge(Schema.givenName, RDF.TYPE, OWL.DATATYPEPROPERTY);
		
		ShapeBuilder builder = new ShapeBuilder(shapeManager);
		builder.beginShape("http://example.com/shapes/PersonShape")
			.beginProperty(Schema.gender)
			.endProperty()
			.beginProperty(Schema.givenName)
			.endProperty()
		.endShape();
		
		ModelValidationReport report = validator.process(request);
		List<URI> violationList = report.getClassPropertyDisjointViolation();
		
		assertEquals(2, violationList.size());
		
		assertTrue(violationList.contains(Schema.Person));
		assertTrue(violationList.contains(Schema.gender));
	}

	@Test
	public void testPropertyShapeCase() {
		
		
		request.getCaseStyle().setProperties(CaseStyle.camelCase);
		
		URI personShape = uri("http://example.com/shapes/PersonShape");
		URI foo = uri("http://example.com/FOO");
		ShapeBuilder builder = new ShapeBuilder(shapeManager);
		builder
			.beginShape(personShape)
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginProperty(foo)
				.endProperty()
			.endShape();
		
		ModelValidationReport report = validator.process(request);
		
		List<PropertyShapeReference> violations = report.getPropertyShapeCaseViolation();
		assertEquals(1, violations.size());
		
		PropertyShapeReference p = violations.get(0);
		assertEquals(personShape, p.getShapeId());
		assertEquals(foo, p.getPredicate());
		
	}
	
	@Test
	public void testNodeShapeCase() {
		
		
		request.getCaseStyle().setNodeShapes(CaseStyle.PascalCase);
		
		URI personShape = uri("http://example.com/shapes/PersonShape");
		URI productShape = uri("http://example.com/shapes/PRODUCT_SHAPE");
		
		ShapeBuilder builder = new ShapeBuilder(shapeManager);
		builder
			.beginShape(personShape)
			.endShape()
			.beginShape(productShape)
			.endShape();
		
		ModelValidationReport report = validator.process(request);
		
		NodeShapeReport personReport = report.findNodeReport(personShape);
		assertTrue(personReport == null);
	
		NodeShapeReport productReport = report.findNodeReport(productShape);
		assertTrue(productReport.getNameHasWrongCase());
	}
	
	@Test
	public void testIndividualCase() {
		
		
		request.getCaseStyle().setNamedIndividuals(CaseStyle.PascalCase);
		
		URI FooBar = uri("http://example.com/FooBar");
		URI foo = uri("http://example.com/foo");
		edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		edge(FooBar, RDFS.SUBCLASSOF, Schema.Enumeration);
		

		edge(Schema.Female, RDF.TYPE, Schema.GenderType);
		edge(Schema.Female, RDFS.COMMENT, literal("The femal gender"));
		edge(foo, RDF.TYPE, FooBar);
		
		
		ModelValidationReport report = validator.process(request);
		
		NamedIndividualReport femaleReport = report.findNamedIndividualReport(Schema.Female);
		assertTrue(femaleReport == null);
		
		NamedIndividualReport fooReport = report.findNamedIndividualReport(foo);
		assertTrue(fooReport != null);
		assertTrue(fooReport.getNameHasWrongCase());
		
	}
	
	@Test
	public void testPropertyCase() {
		
		
		request.getCaseStyle().setProperties(CaseStyle.camelCase);
		
		URI Foo = uri("http://example.com/Foo");
		
		edge(Schema.givenName, RDF.TYPE, RDF.PROPERTY);
		edge(Foo, RDF.TYPE, OWL.DATATYPEPROPERTY);
		
		ModelValidationReport report = validator.process(request);
		
		PropertyReport givenNameReport = report.findPropertyReport(Schema.givenName);
		assertTrue(givenNameReport == null);
		
		PropertyReport fooReport = report.findPropertyReport(Foo);
		assertTrue(fooReport != null);
		assertTrue(fooReport.getNameHasWrongCase());
		
	}

	@Test
	public void testClassCase() {
		
		
		request.getCaseStyle().setClasses(CaseStyle.PascalCase);
		
		URI foo = uri("http://example.com/foo");
		
		edge(Schema.Person, RDF.TYPE, OWL.CLASS);
		edge(Schema.Person, RDFS.COMMENT, literal("A person alive, dead, undead or fictional"));
		edge(foo, RDF.TYPE, OWL.CLASS);
		
		ModelValidationReport report = validator.process(request);
		ClassReport personReport = report.findClassReport(Schema.Person);
		assertTrue(personReport == null);
		
		ClassReport fooReport = report.findClassReport(foo);
		assertTrue(fooReport != null);
		assertTrue(fooReport.getNameHasWrongCase());
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void edge(URI subject, URI predicate, Value object) {
		graph.edge(subject, predicate, object);
	}

}

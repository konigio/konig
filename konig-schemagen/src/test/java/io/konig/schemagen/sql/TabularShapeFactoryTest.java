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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.vocab.Schema;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeWriter;

public class TabularShapeFactoryTest {
	private static final URI EMAIL = new URIImpl("http://example.com/ns/alias/EMAIL");


	private static final Integer ONE = new Integer(1);
	private String aliasNamespace = "http://example.com/ns/alias/";
	private ShapeManager shapeManager = new MemoryShapeManager();
	private TabularShapeFactory generator = new TabularShapeFactory(shapeManager, aliasNamespace);
	
	@Test
	public void testPersonWithOneSponsorReference() throws Exception {

		URI personShapeId = uri("http://example.com/shapes/PersonShape");
		URI tabularPersonShapeId = uri("http://example.com/shapes/TabularPersonShape");
		
		
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.nodeKind(NodeKind.IRI)
			.beginProperty(Schema.sponsor)
				.maxCount(1)
				.nodeKind(NodeKind.IRI)
				.valueClass(Schema.Organization)
			.endProperty()
		.endShape()
		.beginShape(tabularPersonShapeId)
			.tabularOriginShape(personShapeId)
		.endShape()
		;
		
		Shape tabularShape = builder.getShape(tabularPersonShapeId);
		
		generator.process(tabularShape);
		
		
		
		ShapeWriter.write(System.out, tabularShape);
		
		
		// TODO: Add more validation
	}
	
	@Ignore
	public void testPersonWithManyAffliationReferences() throws Exception {

		URI personShapeId = uri("http://example.com/shapes/PersonShape");
		URI tabularPersonShapeId = uri("http://example.com/shapes/TabularPersonShape");
		
		URI tabularAffiliationShapeId = uri("http://example.com/shapes/TABULAR_PERSON_AFFILIATION_SHAPE");
		
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.nodeKind(NodeKind.IRI)
			.beginProperty(Schema.affiliation)
				.nodeKind(NodeKind.IRI)
				.valueClass(Schema.Organization)
			.endProperty()
		.endShape()
		.beginShape(tabularPersonShapeId)
			.tabularOriginShape(personShapeId)
		.endShape()
		;
		
		Shape tabularShape = builder.getShape(tabularPersonShapeId);
		
		List<Shape> result = generator.process(tabularShape);
		assertEquals(1, result.size());
		
		Shape tabularAffiliationShape = shapeManager.getShapeById(tabularAffiliationShapeId);
		
		
//		ShapeWriter.write(System.out, tabularAffiliationShape);
		
		
		// TODO: Add more validation
	}
	
	@Ignore
	public void testNamedPersonWithManyAddresses() throws Exception {
		URI personShapeId = uri("http://example.com/shapes/PersonShape");
		URI tabularPersonShapeId = uri("http://example.com/shapes/TabularPersonShape");
		URI addressShapeId = uri("http://example.com/shapes/AddressShape");
		URI countryShapeId = uri("http://exmaple.com/shapes/CountryShape");
		
		URI tabularAddresShapeId = uri("http://example.com/shapes/TABULAR_PERSON_ADDRESS_SHAPE");
		
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.nodeKind(NodeKind.IRI)
			.beginProperty(Schema.address)
				.beginValueShape(addressShapeId)
					.beginProperty(Schema.postalCode)
						.minCount(1)
						.maxCount(1)
						.datatype(XMLSchema.STRING)
					.endProperty()

					.beginProperty(Schema.addressCountry)
						.minCount(1)
						.maxCount(1)
						.beginValueShape(countryShapeId)
							.beginProperty(Schema.name)
								.minCount(1)
								.maxCount(1)
								.datatype(XMLSchema.STRING)
							.endProperty()
						.endValueShape()
					.endProperty()
					
				.endValueShape()
			.endProperty()
		.endShape()
		.beginShape(tabularPersonShapeId)
			.tabularOriginShape(personShapeId)
		.endShape()
		;
		
		Shape tabularShape = builder.getShape(tabularPersonShapeId);
		
		List<Shape> result = generator.process(tabularShape);
		assertEquals(1, result.size());
		
		Shape tabularAddressShape = shapeManager.getShapeById(tabularAddresShapeId);
		
		
		ShapeWriter.write(System.out, tabularAddressShape);
		
		
		// TODO: Add more validation
	}
	
	@Ignore
	public void testNamedPersonWithOneAddress() throws Exception {
		URI personShapeId = uri("http://example.com/shapes/PersonShape");
		URI tabularPersonShapeId = uri("http://example.com/shapes/TabularPersonShape");
		URI addressShapeId = uri("http://example.com/shapes/AddressShape");
		URI countryShapeId = uri("http://exmaple.com/shapes/CountryShape");

		
		URI postalCode = uri("http://example.com/ns/alias/ADDRESS__POSTAL_CODE");
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.nodeKind(NodeKind.IRI)
			.beginProperty(Schema.address)
				.maxCount(1)
				.beginValueShape(addressShapeId)
					.beginProperty(Schema.postalCode)
						.minCount(1)
						.maxCount(1)
						.datatype(XMLSchema.STRING)
					.endProperty()

					.beginProperty(Schema.addressCountry)
						.minCount(1)
						.maxCount(1)
						.beginValueShape(countryShapeId)
							.beginProperty(Schema.name)
								.minCount(1)
								.maxCount(1)
								.datatype(XMLSchema.STRING)
							.endProperty()
						.endValueShape()
					.endProperty()
					
				.endValueShape()
			.endProperty()
		.endShape()
		.beginShape(tabularPersonShapeId)
			.tabularOriginShape(personShapeId)
		.endShape()
		;
		
		Shape tabularShape = builder.getShape(tabularPersonShapeId);
		
		generator.process(tabularShape);
		
//		ShapeWriter.write(System.out, tabularShape);
		
		assertEquals(ONE, tabularShape.getPropertyConstraint(postalCode).getMinCount());
		assertEquals(ONE, tabularShape.getPropertyConstraint(postalCode).getMaxCount());
		assertEquals(XMLSchema.STRING, tabularShape.getPropertyConstraint(postalCode).getDatatype());
		
		// TODO: Add more validation
	}

	@Ignore
	public void testNamedPersonWithOneEmail() throws Exception {
		URI personShapeId = uri("http://example.com/shapes/PersonShape");
		URI tabularPersonShapeId = uri("http://example.com/shapes/TabularPersonShape");
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.nodeKind(NodeKind.IRI)
			.beginProperty(Schema.email)
				.datatype(XMLSchema.STRING)
				.maxCount(1)
			.endProperty()
		.endShape()
		.beginShape(tabularPersonShapeId)
			.targetClass(Schema.Person)
			.tabularOriginShape(personShapeId)
		.endShape()
		;
		
		Shape tabularShape = builder.getShape(tabularPersonShapeId);
		
		generator.process(tabularShape);
		
		PropertyConstraint p = tabularShape.getPropertyConstraint(EMAIL);
		assertTrue(p != null);
		assertEquals(new Integer(1), p.getMaxCount());
	}
	

	@Ignore
	public void testNamedPersonWithManyEmails() throws Exception {
		URI personShapeId = uri("http://example.com/shapes/PersonShape");
		URI tabularPersonShapeId = uri("http://example.com/shapes/TabularPersonShape");
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.nodeKind(NodeKind.IRI)
			.beginProperty(Schema.email)
				.datatype(XMLSchema.STRING)
			.endProperty()
		.endShape()
		.beginShape(tabularPersonShapeId)
			.tabularOriginShape(personShapeId)
			.beginDataSource(GoogleCloudSqlTable.Builder.class)
				.instance("sample_instance")
				.database("sample_database")
				.name("Person")
			.endDataSource()
		.endShape()
		;
		
		Shape tabularShape = builder.getShape(tabularPersonShapeId);
		
		List<Shape> result = generator.process(tabularShape);
		
		PropertyConstraint p = tabularShape.getPropertyConstraint(EMAIL);
		assertTrue(p == null);
		
		assertEquals(1, result.size());
		
		Shape emailShape =  result.get(0);
		URI emailAlias = uri("http://example.com/ns/alias/EMAIL");
		
		
		assertEquals(RDF.STATEMENT, emailShape.getTargetClass());
		assertEquals(ONE, emailShape.getDerivedPropertyByPredicate(RDF.SUBJECT).getMaxCount());
		assertEquals(ONE, emailShape.getDerivedPropertyByPredicate(RDF.SUBJECT).getMinCount());
		assertEquals(Schema.Person, emailShape.getDerivedPropertyByPredicate(RDF.SUBJECT).getValueClass());
		

		assertEquals(ONE, emailShape.getDerivedPropertyByPredicate(RDF.PREDICATE).getMaxCount());
		assertEquals(ONE, emailShape.getDerivedPropertyByPredicate(RDF.PREDICATE).getMinCount());
		assertEquals(Schema.email, emailShape.getDerivedPropertyByPredicate(RDF.PREDICATE).getIn().get(0));

		assertEquals(ONE, emailShape.getDerivedPropertyByPredicate(RDF.OBJECT).getMaxCount());
		assertEquals(ONE, emailShape.getDerivedPropertyByPredicate(RDF.OBJECT).getMinCount());
		assertEquals(emailAlias, emailShape.getDerivedPropertyByPredicate(RDF.OBJECT).getEquals());
		
		URI personId = uri("http://example.com/ns/alias/PERSON_FK");
		assertEquals(ONE, emailShape.getPropertyConstraint(personId).getMinCount());
		assertEquals(ONE, emailShape.getPropertyConstraint(personId).getMaxCount());
		
		assertEquals(ONE, emailShape.getPropertyConstraint(emailAlias).getMinCount());
		assertEquals(ONE, emailShape.getPropertyConstraint(emailAlias).getMaxCount());
		assertEquals(XMLSchema.STRING, emailShape.getPropertyConstraint(emailAlias).getDatatype());
		
		GoogleCloudSqlTable table = (GoogleCloudSqlTable) emailShape.getShapeDataSource().get(0);
		assertEquals(uri("https://www.googleapis.com/sql/v1beta4/projects/${gcpProjectId}/instances/sample_instance/databases/sample_database/tables/PERSON_EMAIL"), table.getId());
		assertEquals("PERSON_EMAIL", table.getName());
		assertEquals("sample_instance", table.getInstance());
		assertEquals("sample_database", table.getDatabase());
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}


}

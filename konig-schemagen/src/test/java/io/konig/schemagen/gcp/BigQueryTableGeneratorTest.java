package io.konig.schemagen.gcp;

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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class BigQueryTableGeneratorTest {
	
	private ShapeBuilder builder = new ShapeBuilder();
	private Graph graph = new MemoryGraph();
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private BigQueryTableGenerator generator = new BigQueryTableGenerator(builder.getShapeManager(), null, reasoner);
	
	@Test
	public void testEmbeddedXone() throws IOException {

		URI agentShapeId = uri("http://example.com/shapes/AgentShape");
		URI personShapeId = uri("http://example.com/shapes/PersonShape");
		URI orgShapeId = uri("http://example.com/shapes/OrgShape");
		
		builder
		.beginShape(personShapeId) 
			.targetClass(Schema.Person)
			.beginProperty(Schema.address)
				.maxCount(1)
				.minCount(1)
				.beginValueShape("http://example.com/shapes/PersonAddressShape")
					.beginProperty(Schema.addressLocality)
						.minCount(1)
						.maxCount(1)
					.endProperty()
					.beginProperty(Schema.addressCountry)
						.minCount(1)
						.maxCount(1)
					.endProperty()
				.endValueShape()
			.endProperty()
		.endShape()
		.beginShape(orgShapeId)
			.targetClass(Schema.Organization)
			.beginProperty(Schema.address)
				.maxCount(1)
				.minCount(1)
				.beginValueShape("http://example.com/shapes/OrganizationAddressShape")
					.beginProperty(Schema.addressRegion)
						.minCount(1)
						.maxCount(1)
					.endProperty()
					.beginProperty(Schema.addressCountry)
						.minCount(1)
						.maxCount(1)
					.endProperty()
				.endValueShape()
			.endProperty()
		.endShape()
		.beginShape(agentShapeId)
			.targetClass(FOAF.AGENT)
			.xone(personShapeId, orgShapeId)
		.endShape();
		

		Shape shape = builder.getShape(agentShapeId);
		
		TableSchema table = generator.toTableSchema(shape);
		TableFieldSchema address = field(table.getFields(), "address");
		assertEquals("REQUIRED", address.getMode());
		
		TableFieldSchema addressLocality = field(address.getFields(), "addressLocality");
		assertEquals("NULLABLE", addressLocality.getMode());
		
		TableFieldSchema addressRegion = field(address.getFields(), "addressRegion");
		assertEquals("NULLABLE", addressRegion.getMode());
		
		TableFieldSchema addressCountry = field(address.getFields(), "addressCountry");
		assertEquals("REQUIRED", addressCountry.getMode());
	}
	
	@Test
	public void testXone() {
		
		URI agentShapeId = uri("http://example.com/shapes/AgentShape");
		URI personShapeId = uri("http://example.com/shapes/PersonShape");
		URI orgShapeId = uri("http://example.com/shapes/OrgShape");
		
		builder
		.beginShape(personShapeId) 
			.targetClass(Schema.Person)
			.beginProperty(Schema.givenName)
				.maxCount(1)
				.minCount(1)
			.endProperty()
			.beginProperty(Schema.email)
				.datatype(XMLSchema.STRING)
				.minCount(1)
				.maxCount(1)
			.endProperty()
		.endShape()
		.beginShape(orgShapeId)
			.targetClass(Schema.Organization)
			.beginProperty(Schema.name)
				.maxCount(1)
				.minCount(1)
			.endProperty()
			.beginProperty(Schema.email)
				.datatype(XMLSchema.STRING)
				.minCount(1)
				.maxCount(1)
			.endProperty()
		.endShape()
		.beginShape(agentShapeId)
			.targetClass(FOAF.AGENT)
			.xone(personShapeId, orgShapeId)
		.endShape();
		

		Shape shape = builder.getShape(agentShapeId);
		
		TableSchema table = generator.toTableSchema(shape);
		

		TableFieldSchema name = field(table.getFields(), "name");
		assertEquals("NULLABLE", name.getMode());
		
		TableFieldSchema givenName = field(table.getFields(), "givenName");
		assertEquals("NULLABLE", givenName.getMode());
		
		TableFieldSchema email = field(table.getFields(), "email");
		assertEquals("REQUIRED", email.getMode());
	}

	@Test
	public void testLangString() {
		
		URI shapeId = uri("http://example.com/shapes/ProductShape");
		
		builder
		.beginShape(shapeId)
			.beginProperty(Schema.name)
				.maxCount(1)
				.datatype(RDF.LANGSTRING)
			.endProperty()
		.endShape();
		
		Shape shape = builder.getShape(shapeId);
		
		TableSchema table = generator.toTableSchema(shape);
		
		TableFieldSchema name = field(table.getFields(), "name");
		
		assertEquals("RECORD", name.getType());
		
		TableFieldSchema stringValue = field(name.getFields(), "stringValue");
		assertEquals("STRING", stringValue.getType());
		assertEquals("REQUIRED", stringValue.getMode());
		
		TableFieldSchema languageCode = field(name.getFields(), "languageCode");
		assertEquals("STRING", languageCode.getType());
		assertEquals("REQUIRED", languageCode.getMode());
	}

	private TableFieldSchema field(List<TableFieldSchema> fieldList, String fieldName) {
		Optional<TableFieldSchema> result = fieldList.stream().filter(f -> fieldName.equals(f.getName())).findAny();
		assertTrue(result.isPresent());
		return result.get();
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

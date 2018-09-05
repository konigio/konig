package io.konig.schemagen.jsonschema;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.ContextManager;
import io.konig.core.GraphBuilder;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryContextManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.schemagen.jsonschema.impl.SimpleJsonSchemaNamer;
import io.konig.schemagen.jsonschema.impl.SimpleJsonSchemaTypeMapper;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMediaTypeNamer;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;
import io.konig.shacl.io.ShapeLoader;

public class JsonSchemaGeneratorTest {

	ShapeMediaTypeNamer mediaTypeNamer = new SimpleShapeMediaTypeNamer();
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private MemoryGraph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();

	JsonSchemaNamer namer = new SimpleJsonSchemaNamer("/json-schema", mediaTypeNamer);
	JsonSchemaTypeMapper typeMapper = new SimpleJsonSchemaTypeMapper();
	JsonSchemaGenerator generator = new JsonSchemaGenerator(namer, nsManager, typeMapper);
	
	@Test
	public void testObjectArray() throws Exception {

		load("src/test/resources/object-array");
		URI shapeId = uri("https://schema.pearson.com/shapes/PiiPersonV1Shape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		ObjectNode schema = generator.generateJsonSchema(shape);
		
		JsonNode identifiedBy = schema.get("properties").get("identifiedBy");
		assertEquals("array", identifiedBy.get("type").asText());
		JsonNode items = identifiedBy.get("items");
		assertEquals("#/definitions/PiiIdentityShape", items.get("$ref").asText());
		
		JsonNode identityShape = schema.get("definitions").get("PiiIdentityShape");
		assertTrue(identityShape != null);
		assertEquals("object", identityShape.get("type").asText());
		assertTrue(identityShape.get("properties") != null);
		
		
//		ObjectMapper mapper = new ObjectMapper();
//		
//		mapper.enable(SerializationFeature.INDENT_OUTPUT);
//		mapper.setSerializationInclusion(Include.NON_NULL);
//		String actualJson = mapper.writeValueAsString(schema);
//		
//		
//		System.out.println(actualJson);
	}
	
	@Test
	public void testOrConstraint() throws Exception {

		URI partyShapeId = uri("http://example.com/shapes/v1/schema/PartyShape");
		URI personShapeId = uri("http://example.com/shapes/v1/schema/PersonShape");
		URI orgShapeId = uri("http://example.com/shapes/v1/schema/OrganizationShape");
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		
		shapeBuilder
		
			.beginShape(partyShapeId)
				.or(personShapeId, orgShapeId)
			.endShape()
			
			.beginShape(personShapeId)
				.beginProperty(Schema.familyName)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
			.endShape()
			
			.beginShape(orgShapeId)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)				
				.endProperty()
			.endShape()
			;
		
		ShapeManager shapeManager = shapeBuilder.getShapeManager();

		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");

		ShapeMediaTypeNamer mediaTypeNamer = new SimpleShapeMediaTypeNamer();
		
		JsonSchemaNamer namer = new SimpleJsonSchemaNamer("/json-schema", mediaTypeNamer);
		JsonSchemaTypeMapper typeMapper = new SimpleJsonSchemaTypeMapper();
		JsonSchemaGenerator generator = new JsonSchemaGenerator(namer, nsManager, typeMapper);
		
		Shape shape = shapeManager.getShapeById(partyShapeId);
		ObjectNode node = generator.generateJsonSchema(shape);
		
		ArrayNode anyOf = (ArrayNode) node.get("anyOf");
		assertEquals(2, anyOf.size());
		ObjectNode personShape = (ObjectNode) anyOf.get(0);
		ObjectNode properties = (ObjectNode) personShape.get("properties");
		ObjectNode familyName = (ObjectNode) properties.get("familyName");
		assertEquals("string", familyName.get("type").asText());
		
		ObjectNode orgShape = (ObjectNode) anyOf.get(1);
		properties = (ObjectNode) orgShape.get("properties");
		ObjectNode name = (ObjectNode) properties.get("name");
		assertEquals("string", name.get("type").asText());
		
		
		
	}

	@Test
	public void test() {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("institution", "http://www.konig.io/institution/");
		
		URI addressShapeId = uri("http://www.konig.io/shapes/v1/schema/Address");
		URI shapeId = uri("http://www.konig.io/shapes/v1/schema/Person");
		
		MemoryGraph graph = new MemoryGraph();
		GraphBuilder builder = new GraphBuilder(graph);
		builder.beginSubject(uri("http://www.konig.io/institution/Stanford"))
			.addProperty(RDF.TYPE, Schema.Organization)
		.endSubject()
		.beginSubject(uri("http://www.konig.io/institution/Princeton"))
			.addProperty(RDF.TYPE, Schema.Organization)
		.endSubject()
		.beginSubject(shapeId)
			.addProperty(RDF.TYPE, SH.Shape)
			.beginBNode(SH.property)
				.addProperty(SH.path, RDF.TYPE)
				.addProperty(SH.hasValue, uri("http://schema.org/Person"))
				.addProperty(SH.maxCount, 1)
				.addProperty(SH.minCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.path, Schema.givenName)
				.addProperty(SH.datatype, XMLSchema.STRING)
				.addLiteral(RDFS.COMMENT, "The person's given name")
				.addProperty(SH.minCount, 1)
				.addProperty(SH.maxCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.path, Schema.address)
				.addProperty(SH.shape, addressShapeId)
				.addProperty(SH.minCount, 1)
				.addProperty(SH.maxCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.path, Schema.memberOf)
				.addProperty(SH.valueClass, Schema.Organization)
				.addProperty(SH.nodeKind, SH.IRI)
				.addProperty(SH.minCount, 0)
			.endSubject()
		.endSubject();
		
		ContextManager contextManager = new MemoryContextManager();
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(contextManager, shapeManager);
		ShapeMediaTypeNamer mediaTypeNamer = new SimpleShapeMediaTypeNamer();
		loader.load(graph);
		
		
		JsonSchemaNamer namer = new SimpleJsonSchemaNamer("/json-schema", mediaTypeNamer);
		Shape shape = shapeManager.getShapeById(shapeId);

		JsonSchemaTypeMapper typeMapper = new SimpleJsonSchemaTypeMapper();
		JsonSchemaGenerator generator = new JsonSchemaGenerator(namer, nsManager, typeMapper);
		ObjectNode json = generator.generateJsonSchema(shape);
		
//		String id = json.get("id").asText();
//		assertEquals("http://www.konig.io/shapes/v1/schema/Person/json-schema", id);
		assertEquals("object", json.get("type").asText());
		
		ObjectNode properties = (ObjectNode) json.get("properties");
		assertTrue(properties != null);
		
		ObjectNode givenName = (ObjectNode) properties.get("givenName");
		assertTrue(givenName != null);
		assertEquals("The person's given name", givenName.get("description").asText());
		
		ObjectNode memberOf = (ObjectNode) properties.get("memberOf");
		assertTrue(memberOf != null);
		assertEquals("array", memberOf.get("type").asText());
		ObjectNode memberOfType = (ObjectNode) memberOf.get("items");
		assertEquals("string", memberOfType.get("type").asText());
		assertEquals("uri", memberOfType.get("format").asText());
		
	}
	

	
	private URI uri(String value) {
		return new URIImpl(value);
	}


	private void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		
		File file = new File(path);
		RdfUtil.loadTurtle(file, graph, shapeManager);
	}


}

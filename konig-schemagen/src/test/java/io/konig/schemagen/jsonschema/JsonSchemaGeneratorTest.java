package io.konig.schemagen.jsonschema;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.ContextManager;
import io.konig.core.GraphBuilder;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryContextManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.schemagen.jsonschema.impl.SimpleJsonSchemaNamer;
import io.konig.schemagen.jsonschema.impl.SimpleJsonSchemaTypeMapper;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMediaTypeNamer;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;
import io.konig.shacl.io.ShapeLoader;

public class JsonSchemaGeneratorTest {

	@Test
	public void test() {
		
		JsonSchemaTypeMapper typeMapper = new SimpleJsonSchemaTypeMapper();
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
				.addProperty(SH.predicate, RDF.TYPE)
				.addProperty(SH.hasValue, uri("http://schema.org/Person"))
				.addProperty(SH.maxCount, 1)
				.addProperty(SH.minCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.predicate, Schema.givenName)
				.addProperty(SH.datatype, XMLSchema.STRING)
				.addLiteral(RDFS.COMMENT, "The person's given name")
				.addProperty(SH.minCount, 1)
				.addProperty(SH.maxCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.predicate, Schema.address)
				.addProperty(SH.valueShape, addressShapeId)
				.addProperty(SH.minCount, 1)
				.addProperty(SH.maxCount, 1)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.predicate, Schema.memberOf)
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
		
		JsonSchemaGenerator generator = new JsonSchemaGenerator(namer, nsManager, typeMapper);
		ObjectNode json = generator.generateJsonSchema(shape);
		
		String id = json.get("id").asText();
		assertEquals("http://www.konig.io/shapes/v1/schema/Person/json-schema", id);
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

}

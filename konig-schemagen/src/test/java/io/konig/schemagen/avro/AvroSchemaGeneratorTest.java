package io.konig.schemagen.avro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.GraphBuilder;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.impl.JsonUtil;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.schemagen.avro.impl.SimpleAvroDatatypeMapper;
import io.konig.schemagen.avro.impl.SimpleAvroNamer;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class AvroSchemaGeneratorTest {
	
	@Test
	public void testIn() {
		URI shapeId = uri("http://example.com/shape/FooShape");
		
		ShapeBuilder builder = new ShapeBuilder();
		
		builder
			.beginShape(shapeId)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.maxCount(1)
					.in("foo", "bar")
				.endProperty()
			.endShape();
		
		Shape shape = builder.getShape(shapeId);
		
		AvroSchemaGenerator avro = generator();
		ObjectNode avroSchema = avro.generateSchema(shape);

		JsonUtil.prettyPrint(avroSchema, System.out);

		JsonValidator validator = new JsonValidator(avroSchema);
		validator
			.beginArray("fields")
				.firstObject()
					.beginObject("type")
						.assertField("type", "enum")
						.assertField("name", "NameEnum")
						.beginArray("symbols")
							.firstString("foo")
							.nextString("bar")
						.endArray()
					.endObject()
				.endObject()
			.endArray();
	}
	
	@Test
	public void testValueShape() {

		URI shapeId = uri("http://example.com/shape/PersonShape");
		URI addressShapeId = uri("http://example.com/shape/AddressShape");
		
		ShapeBuilder builder = new ShapeBuilder();
		builder
			.beginShape(shapeId)
				.beginProperty(Schema.address)
					.beginValueShape(addressShapeId)
						.beginProperty(Schema.streetAddress)
							.datatype(XMLSchema.STRING)
							.maxCount(1)
						.endProperty()
						.beginProperty(Schema.addressLocality)
							.datatype(XMLSchema.STRING)
							.maxCount(1)
						.endProperty()
					.endValueShape()
				.endProperty()
		
			.endShape();


		Shape shape = builder.getShape(shapeId);
		
		AvroSchemaGenerator avro = generator();
		ObjectNode avroSchema = avro.generateSchema(shape);
		
		JsonValidator validator = new JsonValidator(avroSchema);
		validator
			.assertField("type", "record")
			.beginArray("fields")
				.firstObject()
					.assertField("name", "address")
					.beginObject("type")
						.assertField("type", "array")
						.beginObject("items")
							.assertField("name", "com.example.shape.AddressShape")
							.assertField("type", "record")
							.beginArray("fields")
								.firstObject()
									.assertField("name", "streetAddress")
									.assertField("type", "string")
								.nextObject()
									.assertField("name", "addressLocality")
									.assertField("type", "string")
								.endObject()
							.endArray()
						.endObject()
					.endObject()
				.endObject()
			.endArray();
		
	}
	
	@Test 
	public void testSimpleFields() {
		
		URI shapeId = uri("http://example.com/shape/Foo");
		URI count = uri("http://example.com/schema/count");
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(shapeId)
			.beginProperty(Schema.name)
				.datatype(XMLSchema.STRING)
				.minCount(1)
				.maxCount(1)
			.endProperty()
			.beginProperty(count)
				.datatype(XMLSchema.INT)
				.minCount(1)
				.maxCount(1)
			.endProperty()
			.beginProperty(AS.endTime)
				.datatype(XMLSchema.DATETIME)
				.minCount(1)
				.maxCount(1)
			.endProperty()
		.endShape();
		

		Shape shape = builder.getShape(shapeId);
		
		AvroSchemaGenerator avro = generator();
		ObjectNode avroSchema = avro.generateSchema(shape);
		
		JsonValidator validator = new JsonValidator(avroSchema);
		
		validator
			.assertField("name", "com.example.shape.Foo")
			.assertField("type", "record")
			.beginArray("fields")
				.firstObject()
					.assertField("name", "name")
					.assertField("type", "string")
				.nextObject()
					.assertField("name", "count")
					.assertField("type", "int")
				.nextObject()
					.assertField("name", "endTime")
					.beginObject("type")
						.assertField("type", "long")
						.assertField("logicalType", "timestamp-millis")
					.endObject()
				.endObject()
			.endArray();
		
		
		
		
		JsonUtil.prettyPrint(avroSchema, System.out);
		
	}
	
	
	private AvroSchemaGenerator generator() {
		AvroNamer namer = new SimpleAvroNamer();
		AvroDatatypeMapper datatypeMapper = new SimpleAvroDatatypeMapper();
		
		return new AvroSchemaGenerator(datatypeMapper, namer, null);
	}


	@Test
	public void testBNodeOrIRINodeKind() throws Exception {
		URI shapeId = uri("http://example.com/shapes/v1/schema/Person");
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");

		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(shapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(SH.targetClass, Schema.Person)
				.addProperty(SH.nodeKind, SH.BlankNodeOrIRI)
			.endSubject();
		
		AvroNamer namer = new SimpleAvroNamer();

		Vertex shapeVertex = graph.getVertex(shapeId);
		
		AvroSchemaGenerator avro = new AvroSchemaGenerator(new SimpleAvroDatatypeMapper(), namer, nsManager);
		
		AvroSchemaResource resource = avro.generateSchema(shapeVertex);
		
		ObjectNode schema = resource.toObjectNode();
		ArrayNode fieldArray = (ArrayNode) schema.get("fields");
		assertTrue(fieldArray != null);
		
		JsonNode idField = getField(fieldArray, "id");
		JsonNode idValue = idField.get("type");
		assertTrue(idValue instanceof ArrayNode);
	
		assertEquals(2, idValue.size());
		
		JsonNode firstValue = idValue.get(0);
		assertEquals(firstValue.asText(), "null");
		
		JsonNode secondValue = idValue.get(1);
		assertEquals(secondValue.asText(), "string");
	}
	
	@Test
	public void testIRINodeKind() throws Exception {
		URI shapeId = uri("http://example.com/shapes/v1/schema/Person");
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");

		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(shapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(SH.targetClass, Schema.Person)
				.addProperty(SH.nodeKind, SH.IRI)
			.endSubject();
		
		AvroNamer namer = new SimpleAvroNamer();

		Vertex shapeVertex = graph.getVertex(shapeId);
		
		AvroSchemaGenerator avro = new AvroSchemaGenerator(new SimpleAvroDatatypeMapper(), namer, nsManager);
		
		AvroSchemaResource resource = avro.generateSchema(shapeVertex);
		
		ObjectNode schema = resource.toObjectNode();
		ArrayNode fieldArray = (ArrayNode) schema.get("fields");
		assertTrue(fieldArray != null);
		
		ObjectNode idField = getField(fieldArray, "id");
		assertEquals(idField.get("type").asText(), "string");
		
	}
	



	private ObjectNode getField(ArrayNode fieldArray, String name) {
		for (int i=0; i<fieldArray.size(); i++) {
			JsonNode node = fieldArray.get(i);
			if (node instanceof ObjectNode) {
				ObjectNode object = (ObjectNode) node;
				JsonNode nameNode = object.get("name");
				if (nameNode != null && nameNode.asText().equals(name)) {
					return object;
				}
			}
		}
		fail("Field not found: " + name);
		return null;
	}




	@Test
	public void test() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("institution", "http://www.konig.io/institution/");
		
		AvroNamer namer = new SimpleAvroNamer();
		
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
				.addProperty(SH.maxCount, 1)
			.endSubject()
		.endSubject();
		
		Vertex shapeVertex = graph.vertex(shapeId);
		
		AvroSchemaGenerator avro = new AvroSchemaGenerator(new SimpleAvroDatatypeMapper(), namer, nsManager);
		
		AvroSchemaResource resource = avro.generateSchema(shapeVertex);
		
//		System.out.println(resource.getText());
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

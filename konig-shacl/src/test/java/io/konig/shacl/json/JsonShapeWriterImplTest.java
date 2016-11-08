package io.konig.shacl.json;

/*
 * #%L
 * Konig SHACL
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Ignore;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.json.JsonBuilder;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class JsonShapeWriterImplTest {
	
	private static URI BAR = new URIImpl("http://example.com/bar");
	private JsonNodeFactory factory = JsonNodeFactory.instance;
	
	
	private Shape createShape(URI datatype) {
		// Build a shape that we'll use for testing.
		
		String shapeId = "http://example.com/FooShape";
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(shapeId)
			.beginProperty(BAR)
				.datatype(datatype)
				.minCount(0)
				.maxCount(1)
			.endProperty()
		.endShape();
		
		// Get the shape

		Shape shape = builder.getShape(shapeId);
		
		return shape;
	}
	
	private Vertex createVertex(Literal value) {
		URI fooId = uri("http://example.com/foo");
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(fooId)
				.addProperty(BAR, value)
			.endSubject();

		// Get a JSON representation of this 
		Vertex vertex = graph.getVertex(fooId);
		
		return vertex;
		
	}
	
	private ObjectNode expectedJson(JsonNode value) {
		ObjectNode expected = new JsonBuilder()
			.beginObject()
				.put("bar", value)
			.pop();
		
		return expected;
	}
	
	private void simpleAttribute(
		URI fieldType,
		String fieldValue,
		JsonNode jsonValue
		
	) throws Exception {
		// Create the data shape used in this test
		//
		Shape shape = createShape(fieldType);
		
		// Create an RDF graph instance of the data shape
		//
		Vertex vertex = createVertex(literal(fieldValue, fieldType));
		
		// Create the expected JSON structure
		//
		ObjectNode expected = expectedJson(jsonValue);
		
		
		// Use the JsonShapeWriter to serialize the Vertex as a JSON object
		//
		ObjectNode actual = toJsonObject(vertex, shape);
		
		// Verify that the JSON object from the JsonShapeWriter matches
		// the expected JSON object.
		//
		verifyEquals(expected, actual);
	}
	
	@Ignore
	public void testStringAttribute() throws Exception {
		
		simpleAttribute(XMLSchema.STRING, "hello", factory.textNode("hello"));
		
	}
	
	@Ignore
	public void testNumericAttribute() throws Exception {
		simpleAttribute(XMLSchema.DOUBLE, "3.14159", factory.numberNode(3.1459));
	}

	private Literal literal(String label, URI datatype) {
		return new LiteralImpl(label, datatype);
	}

	@Ignore
	public void testPersonWithAddress() throws IOException {
		
		// Build a Shape that we'll use for testing

		URI personShapeId = uri("http://example.com/shape/v1/schema/PersonShape");
		URI addressShapeId = uri("http://example.com/shape/v1/schema/AddressShape");
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
			.targetClass(Schema.Person)
			.beginProperty(Schema.givenName)
				.datatype(XMLSchema.STRING)
				.minCount(1)
				.maxCount(1)
			.endProperty()
			.beginProperty(Schema.familyName)
				.datatype(XMLSchema.STRING)
				.minCount(1)
				.maxCount(1)
			.endProperty()
			.beginProperty(Schema.address)
				.beginValueShape(addressShapeId)
					.beginProperty(Schema.streetAddress)
						.datatype(XMLSchema.STRING)
						.minCount(1)
						.maxCount(1)
					.endProperty()
					.beginProperty(Schema.addressLocality)
						.datatype(XMLSchema.STRING)
						.minCount(1)
						.maxCount(1)
					.endProperty()
					.beginProperty(Schema.addressRegion)
						.datatype(XMLSchema.STRING)
						.minCount(1)
						.maxCount(1)
					.endProperty()
					.beginProperty(Schema.postalCode)
						.datatype(XMLSchema.STRING)
						.minCount(1)
						.maxCount(1)
					.endProperty()
				.endValueShape()
			.endProperty()
		.endShape();
		
		// Get the shape that we just constructed

		Shape shape = builder.getShape(personShapeId.stringValue());
		
		// Create an RDF graph representation of a Person entity
		
		URI aliceId = uri("http://example.com/person/alice");
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(aliceId)
				.addLiteral(Schema.givenName, "Alice")
				.addLiteral(Schema.familyName, "Jones")
				.beginBNode(Schema.address)
					.addLiteral(Schema.streetAddress, "101 Main Street")
					.addLiteral(Schema.addressLocality, "Springfield")
					.addLiteral(Schema.addressRegion, "MA")
					.addLiteral(Schema.postalCode, "01101")
				.endSubject()
			.endSubject();
		
		// From the graph, get the Vertex representing the person named Alice Jones.
		Vertex alice = graph.getVertex(aliceId); 
		
		// Create the expected JSON Object
		ObjectNode expected = new JsonBuilder()
			.beginObject()
				.put("givenName", "Alice")
				.put("familyName", "Jones")
				.beginObject("address")
					.put("streetAddress", "101 Main Street")
					.put("addressLocality", "Springfield")
					.put("addressRegion", "MA")
					.put("postalCode", "01101")
				.endObject()
			.pop();
		
		// Use the JsonShapeWriter to serialize the Vertex into JSON
		ObjectNode actual = toJsonObject(alice, shape);
		
		// Verify that the JSON object from the JsonShapeWriter matches the
		// expected JSON.
		//
		verifyEquals(expected, actual);
		
	}
	

	/**
	 * Compare two JSON structures and verify that they are identical.
	 * This method implements a series of assertions that fail if any 
	 * part of the JSON structures are different.
	 * @param expected The expected JSON structure
	 * @param actual The actual JSON structure
	 */
	private void verifyEquals(ObjectNode expected, ObjectNode actual) {
		
		// TODO: Implement this method!
		fail("Not implemented");
		
	}




	private ObjectNode toJsonObject(Vertex resource, Shape shape) throws IOException {
		JsonShapeWriterImpl writer = new JsonShapeWriterImpl();
		
		return writer.toJson(resource, shape);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

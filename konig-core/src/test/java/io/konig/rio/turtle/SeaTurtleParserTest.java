package io.konig.rio.turtle;

/*
 * #%L
 * Konig Core
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Context;
import io.konig.core.Graph;
import io.konig.core.Term;
import io.konig.core.impl.ChainedContext;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.GraphLoadHandler;
import io.konig.core.json.JsonBuilder;
import io.konig.core.vocab.Schema;

public class SeaTurtleParserTest {
	
	private TestContextHandler contextHandler = new TestContextHandler();
	private SeaTurtleParser parser;
	private Graph graph;
	
	@Before
	public void setUp() {

		graph = new MemoryGraph();
		GraphLoadHandler rdfHandler = new GraphLoadHandler(graph);
		
		parser = new SeaTurtleParser();
		parser.setContextHandler(contextHandler);
		parser.setRDFHandler(rdfHandler);
	}
	
	@Test
	public void testBareLocalName() throws Exception {
		StringBuilder buffer = new StringBuilder();
		
		JsonBuilder builder = new JsonBuilder();
		builder.beginObject()
			.put("schema", "http://schema.org/")
			.put("xsd", XMLSchema.NAMESPACE)
			.beginObject("givenName")
				.put("@id", "schema:givenName")
				.put("@type", "xsd:string")
			.endObject();
		
		buffer.append("@context ");
		buffer.append(builder.toString());
		buffer.append("@prefix person : <http://example.com/person/> .");
		buffer.append("person:alice a schema:Person ;");
		buffer.append("  givenName 'Alice'");
		buffer.append(".\n");
		
		
		String text = buffer.toString();
		
		parse(text);
		
		URI alice = uri("http://example.com/person/alice");
		
		assertTrue(graph.contains(alice, Schema.givenName, literal("Alice")));
	}
	
	@Test
	public void testEmbeddedContext() throws Exception {
		
		String text = loadFile("SeaTurtleParserTest/testEmbeddedContext.sea");
		parse(text);

		URI alice = uri("http://example.com/person/alice");
		URI bob = uri("http://example.com/person/bob");
		URI givenName = uri("http://example.com/givenName");
		
		assertTrue(graph.contains(alice, givenName, literal("Alice")));
		assertTrue(graph.contains(bob, Schema.givenName, literal("Bob")));
		
		
	}
	
	@Test
	public void testTypeCoercion() throws Exception {
		String text = loadFile("SeaTurtleParserTest/testTypeCoercion.sea");
		parse(text);
		
		URI  alice = uri("http://example.com/person/alice");
		
		assertTrue(graph.contains(alice, Schema.birthDate, literal("2001-10-15", XMLSchema.DATE)));
	}

	private String loadFile(String filePath) throws Exception {

		Path path = Paths.get(getClass().getClassLoader().getResource(filePath).toURI());

		byte[] encoded = Files.readAllBytes(path);
		return new String(encoded);
	}

	@Test 
	public void testIriPropertyList() throws Exception {
		String text =
			  "@prefix schema: <http://schema.org/> .\n"
			+ "{ "
			+ "  @id <http://example.com/alice> ;"
			+ "  schema:familyName 'Jones' ;"
			+ "  schema:telephone '555-123-4567'"
			+ "} schema:givenName 'Alice' .";
		
		parse(text);
		URI alice = uri("http://example.com/alice");
		assertTrue(graph.contains(alice, Schema.givenName, literal("Alice")));
		assertTrue(graph.contains(alice, Schema.familyName, literal("Jones")));
		assertTrue(graph.contains(alice, Schema.telephone, literal("555-123-4567")));
	}

	private Literal literal(String text, URI datatype) {
		return new LiteralImpl(text, datatype);
	}

	private Literal literal(String text) {
		return new LiteralImpl(text);
	}
	
	private URI uri(String text) {
		return new URIImpl(text);
	}

	@Test
	public void testContext() throws Exception {
		
		StringBuilder buffer = new StringBuilder();
		
		JsonBuilder builder = new JsonBuilder();
		builder.beginObject()
			.put("schema", "http://schema.org/")
			.put("xsd", XMLSchema.NAMESPACE)
			.beginObject("givenName")
				.put("@id", "schema:givenName")
				.put("@type", "xsd:string")
			.endObject();
		
		buffer.append("@context ");
		buffer.append(builder.toString());
		
		String text = buffer.toString();
		
		parse(text);
		Context context = contextHandler.context;
		assertTrue(context != null);
		context.compile();
		
		Term schema = context.getTerm("schema");
		assertTrue(schema!=null);
		
		assertEquals(Schema.NAMESPACE, schema.getId());
		
		Term givenName = context.getTerm("givenName");
		assertTrue(givenName != null);
		assertEquals(Schema.givenName, givenName.getExpandedId());
		assertEquals(XMLSchema.STRING, givenName.getExpandedType());
		
		
	}
	
	private void parse(String text) throws RDFParseException, RDFHandlerException, IOException {
		StringReader reader = new StringReader(text);
		parser.parse(reader, "");
	}
	
	
	
	static class TestContextHandler implements ContextHandler {
		ChainedContext context;

		@Override
		public void addContext(ChainedContext context) {
			this.context = context;
			
		}

		@Override
		public void removeContext(ChainedContext context) {
		}
		
	}

}

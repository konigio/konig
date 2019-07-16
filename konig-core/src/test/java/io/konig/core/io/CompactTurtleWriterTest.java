package io.konig.core.io;

/*
 * #%L
 * konig-core
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.DepthFirstEdgeIterable;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.core.vocab.Schema;

public class CompactTurtleWriterTest {
	
	@Test
	public void testList() throws Exception {
		Graph graph = new MemoryGraph();
		
		URI alice = uri("http://example.com/alice");
		URI bob = uri("http://example.com/bob");
		URI cathy = uri("http://example.com/cathy");
		
		graph.builder().beginSubject(alice)
			.addList(Schema.parent, bob, cathy)
		.endSubject();
		
		StringWriter writer = new StringWriter();
		RdfUtil.prettyPrintTurtle(graph, writer);
		
		String expected = 
			"<http://example.com/alice> <http://schema.org/parent> (<http://example.com/bob> <http://example.com/cathy>) .";
		String actual = writer.toString();
		assertTrue(actual.contains(expected));
	}
	
	@Test
	public void testList1() throws Exception {
		Graph graph = new MemoryGraph();
	
		List<URI> list = new ArrayList();
		list.add(uri("http://example.com/key1"));
		list.add(uri("http://example.com/key2"));
		graph.edge(uri("http://example.com/alice"), OwlVocab.hasKey, list);
		
		StringWriter writer = new StringWriter();
		RdfUtil.prettyPrintTurtle(graph, writer);
		
		String expected = 
			"<http://example.com/alice> <http://www.w3.org/2002/07/owl#hasKey> (<http://example.com/key1> <http://example.com/key2>) .";
		String actual = writer.toString();
		assertTrue(actual.contains(expected));
	}
	
	@Test
	public void testEscapeUnicode() throws Exception {
		Graph graph = new MemoryGraph();
		String arabicValue = "\u0627\u0644\u0627\u0633\u0645 \u0627\u0644\u0645\u0639\u0637\u0649";
		Literal arabicLiteral = literal(arabicValue, "ar");

		String chineeseValue = "\uD852\uDF62";
		Literal chineeseLiteral = literal(chineeseValue, "zh");
		
		graph.edge(Schema.givenName, RDFS.LABEL, arabicLiteral);
		graph.edge(Schema.givenName, RDFS.LABEL, chineeseLiteral);
		
		StringWriter writer = new StringWriter();
		RdfUtil.prettyPrintTurtle(graph, writer);
		
		String text = writer.toString();
		
		assertTrue(text.contains("\"\\u0627\\u0644\\u0627\\u0633\\u0645 \\u0627\\u0644\\u0645\\u0639\\u0637\\u0649\"@ar"));
		assertTrue(text.contains("\"\\U00024b62\"@zh"));
		
		InputStream input = new ByteArrayInputStream(text.getBytes());
		Graph g = new MemoryGraph();
		RdfUtil.loadTurtle(g, input, "");
		
		Vertex v = g.getVertex(Schema.givenName);
		
		boolean foundChineese = false;
		Set<Value> set = v.getValueSet(RDFS.LABEL);
		for (Value value : set) {
			Literal literal = (Literal) value;
			String label = literal.getLabel();
			String language = literal.getLanguage();
			if ("zh".equals(language)) {
				assertEquals(chineeseValue, label);
				foundChineese = true;
			}
		}
		assertTrue(foundChineese);
		
	}

	private Literal literal(String label, String language) {
		return new LiteralImpl(label, language);
	}

	@Test
	public void testPunctuation() throws Exception {
		
		URI alice = uri("http://example.com/alice");
		URI bob = uri("http://example.com/bob");
		URI stanford = uri("http://example.com/stanford");
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(alice)
				.addLiteral(Schema.givenName, "Alice")
				.beginBNode(Schema.contactPoint)
					.addLiteral(Schema.email, "alice@example.com")
					.addLiteral(Schema.telephone, "555-123-4567")
					.beginBNode(Schema.areaServed) 
						.addLiteral(Schema.name, "South Dakota")
					.endSubject()
				.endSubject()
				.addProperty(Schema.alumniOf, stanford)
			.endSubject()
			.beginSubject(bob)
				.addLiteral(Schema.givenName, "Bob")
			.endSubject();
		
		StringWriter buffer = new StringWriter();
		
		CompactTurtleWriter writer = new CompactTurtleWriter(buffer);
		writer.startRDF();
		writer.handleNamespace("schema", "http://schema.org/");
		
		DepthFirstEdgeIterable sequence = new DepthFirstEdgeIterable(graph);
		for (Edge e : sequence) {
			writer.handleStatement(e);
		}
		
		writer.endRDF();
		
		String actual = buffer.toString().trim().replace("\r", "");
		
		String expected = "@prefix schema: <http://schema.org/> .\n" + 
				"\n" + 
				"<http://example.com/alice> schema:givenName \"Alice\" ; \n" + 
				"	schema:contactPoint  [ \n" + 
				"		schema:email \"alice@example.com\" ; \n" + 
				"		schema:telephone \"555-123-4567\" ; \n" + 
				"		schema:areaServed  [ \n" + 
				"			schema:name \"South Dakota\" ]  ]  ; \n" + 
				"	schema:alumniOf <http://example.com/stanford> . \n" + 
				"\n" + 
				"<http://example.com/bob> schema:givenName \"Bob\" .";
		
		
		assertEquals(expected, actual);
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

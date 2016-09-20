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


import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.DepthFirstEdgeIterable;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;

public class CompactTurtleWriterTest {

	@Test
	public void test() throws Exception {
		
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

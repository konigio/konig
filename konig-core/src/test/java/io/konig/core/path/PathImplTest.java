package io.konig.core.path;

/*
 * #%L
 * Konig Core
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

import java.util.Set;

import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Path;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;

public class PathImplTest {

	@Test
	public void testHasStep() {
		
		URI alice = uri("http://example.com/person/alice");
		URI bob = uri("http://example.com/person/bob");
		Literal smith = literal("Smith");
		URI iphone = uri("http://example.com/product/iphone");
		Literal aliceName = literal("Alice");
		
		MemoryGraph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(alice)
				.addProperty(RDF.TYPE, Schema.Person)
				.addLiteral(Schema.givenName, "Alice")
				.addLiteral(Schema.familyName, "Smith")
			.endSubject()
			.beginSubject(iphone)
				.addProperty(RDF.TYPE, Schema.Product)
				.addLiteral(Schema.name, "iPhone")
			.endSubject()
			.beginSubject(bob)
				.addProperty(RDF.TYPE, Schema.Person)
				.addLiteral(Schema.givenName, "Bob")
				.addLiteral(Schema.familyName, "Smith")
			.endSubject();
		
		Path path = new PathImpl()
			.in(RDF.TYPE).has(Schema.familyName, smith);
		
		Vertex person = graph.getVertex(Schema.Person);
		
		Set<Value> result = path.traverse(person);
		assertEquals(2, result.size());
		assertTrue(result.contains(alice));
		assertTrue(result.contains(bob));
		
		path = new PathImpl()
			.in(RDF.TYPE).has(Schema.givenName, aliceName);
		
		result = path.traverse(person);
		assertEquals(1, result.size());
		assertTrue(result.contains(alice));
	}
	
	private Literal literal(String value) {
		return new LiteralImpl(value);
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

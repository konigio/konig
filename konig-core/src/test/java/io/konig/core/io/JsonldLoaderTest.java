package io.konig.core.io;

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

import java.io.Reader;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.json.JsonBuilder;
import io.konig.core.vocab.Schema;

public class JsonldLoaderTest {

	@Test
	public void test() throws Exception {
		JsonBuilder json = new JsonBuilder();
		Reader reader = json.beginObject()
			.beginObject("@context")
				.put("schema", "http://schema.org/")
				.put("Person", "schema:Person")
				.put("givenName", "schema:givenName")
				.put("familyName", "schema:familyName")
			.endObject()
			.put("@id", "http://example.com/person/alice")
			.put("@type", "Person")
			.put("givenName", "Alice")
			.put("familyName", "Smith")
		.toReader();
		
	
		JsonldLoader loader = new JsonldLoader();
		Graph graph = new MemoryGraph();
		
		loader.load(reader, graph);
		
		URI aliceId = uri("http://example.com/person/alice");
		
		Vertex alice = graph.getVertex(aliceId);
		assertTrue(alice != null);
		assertProperty(alice, RDF.TYPE, Schema.Person);
		assertProperty(alice, Schema.givenName, "Alice");
		assertProperty(alice, Schema.familyName, "Smith");
		
		
	}
	private void assertProperty(Vertex subject, URI predicate, Value object) {
		
		Value value = subject.getValue(predicate);
		assertTrue(value != null);
		assertEquals(object, value);
		
	}

	private void assertProperty(Vertex subject, URI predicate, String object) {
		
		Value value = subject.getValue(predicate);
		assertTrue(value != null);
		assertEquals(object, value.stringValue());
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

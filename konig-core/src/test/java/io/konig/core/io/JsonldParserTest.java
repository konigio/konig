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


import java.io.StringReader;

import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;

public class JsonldParserTest {

	@Test
	public void test() throws Exception {
		
		String data = 
			  "{ \"@context\" : {"
			+ "    \"schema\" : \"http://schema.org/\""
			+ "  },"
			+ "  \"@id\" : \"http://example.com/alice\", "
			+ "  \"@type\" : \"schema:Person\" "
			+ "}";
		
		JsonldParser parser = new JsonldParser(null);
		Graph graph = new MemoryGraph();
		GraphLoadHandler handler = new GraphLoadHandler(graph);
		parser.setRDFHandler(handler);
		
		StringReader reader = new StringReader(data);
		parser.parse(reader, "");
		
		assertTrue(graph.contains(uri("http://example.com/alice"), RDF.TYPE, Schema.Person));
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

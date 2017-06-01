package io.konig.core.util;

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

import java.util.StringTokenizer;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;

public class ClassHierarchyPathsTest {

	private Graph graph = new MemoryGraph();
	private Vertex targetClass = graph.vertex("http://example.com/target");

	@Test
	public void test() {
		
		add("A/1/a");
		add("A/2/b");
		add("B/1/a");
		add("B/1/b");
		
		ClassHierarchyPaths paths = new ClassHierarchyPaths(targetClass);
		String expected = 
			 "[[http://example.com/target, urn:A/1/a, urn:A/1, urn:A], "
			+ "[http://example.com/target, urn:A/2/b, urn:A/2, urn:A], "
			+ "[http://example.com/target, urn:B/1/a, urn:B/1, urn:B], "
			+ "[http://example.com/target, urn:B/1/b, urn:B/1, urn:B]]";
		
		assertEquals(expected, paths.toString());
	}

	private void add(String path) {
		StringTokenizer tokens = new StringTokenizer(path, "/");
		
		String id = "urn:";
		
		URI superType = OWL.THING;
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			id += token;
			
			URI uri = uri(id);
			graph.edge(uri, RDFS.SUBCLASSOF, superType);
			superType = uri;
			id += "/";
		}
		graph.edge(targetClass.getId(), RDFS.SUBCLASSOF, superType);
		
	}

	private URI uri(String value) {
		
		return new URIImpl(value);
	}

}

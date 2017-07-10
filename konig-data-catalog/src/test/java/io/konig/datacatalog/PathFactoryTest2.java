package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;

public class PathFactoryTest2 {
	
	@Test
	public void testRelativePathSameFolder() throws Exception {

		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");

		Graph graph = new MemoryGraph(nsManager);
		graph.edge(Schema.Person, RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.CreativeWork, RDF.TYPE, OWL.CLASS);

		OwlReasoner reasoner = new OwlReasoner(graph);
		PathFactory factory = new PathFactory(reasoner, nsManager);
		
		String actual = factory.relativePath(Schema.Person, Schema.CreativeWork);
		String expected = "CreativeWork.html";
		assertEquals(expected, actual);
		
	}
	
	@Test
	public void testRelativePath() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("ex", "http://example.com/ns/");
		nsManager.add("schema", "http://schema.org/");
		Graph graph = new MemoryGraph(nsManager);

		URI category = uri("http://example.com/ns/Category");
		URI target = uri("http://example.com/ns/Electronics/Phone");
		
		graph.edge(category, RDF.TYPE, RDFS.CLASS);
		graph.edge(category, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.CreativeWork, RDF.TYPE, OWL.CLASS);
		
		
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		PathFactory factory = new PathFactory(reasoner, nsManager);
		
		String actual = factory.relativePath(target, Schema.CreativeWork);
		assertEquals("../../schema/classes/CreativeWork.html", actual);
		
	}
	
	@Test
	public void testPagePath() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("ex", "http://example.com/ns/");
		nsManager.add("schema", "http://schema.org/");
		Graph graph = new MemoryGraph(nsManager);

		URI category = uri("http://example.com/ns/Category");
		URI target = uri("http://example.com/ns/Electronics/Phone");
		
		graph.edge(category, RDF.TYPE, RDFS.CLASS);
		graph.edge(category, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(target, RDF.TYPE, category);
		
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		PathFactory factory = new PathFactory(reasoner, nsManager);
		
		String path = factory.pagePath(target);
		assertEquals("ex/individuals/Electronics/Phone.html", path);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

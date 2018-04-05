package io.konig.core;

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

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.ORG;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;

public class OwlReasonerTest {
	
	@Test
	public void testRangeIncludes() {
		Graph graph = new MemoryGraph();
		
		graph.builder()
		.beginSubject(Schema.AudioObject)
			.addProperty(RDFS.SUBCLASSOF, Schema.MediaObject)
		.endSubject()
		
		.beginSubject(Schema.MediaObject)
			.addProperty(RDFS.SUBCLASSOF, Schema.CreativeWork)
		.endSubject()
		
		.beginSubject()
			.beginBNode(SH.property)
				.addProperty(SH.path, Schema.exampleOfWork)
				.addProperty(SH.valueClass, Schema.AudioObject)
			.endSubject()
		.endSubject()
		
		.beginSubject()
			.beginBNode(SH.property)
				.addProperty(SH.path, Schema.exampleOfWork)
				.addProperty(SH.valueClass, Schema.CreativeWork)
			.endSubject()
		.endSubject()

		.beginSubject()
			.beginBNode(SH.property)
				.addProperty(SH.path, Schema.exampleOfWork)
				.beginBNode(SH.shape)
					.addProperty(SH.targetClass, Schema.Product)
				.endSubject()
			.endSubject()
		.endSubject()
		
		;
		
		OwlReasoner owl = new OwlReasoner(graph);
		
		Set<URI> set = owl.rangeIncludes(Schema.exampleOfWork);
		
		assertTrue(set.contains(Schema.CreativeWork));
		assertTrue(!set.contains(Schema.AudioObject));
		assertTrue(set.contains(Schema.Product));
		
	}
	
	@Test
	public void testLeastCommonSubclass() {

		Graph graph = new MemoryGraph();
		graph.edge(Schema.VideoObject, RDFS.SUBCLASSOF, Schema.MediaObject);
		graph.edge(Schema.MediaObject, RDFS.SUBCLASSOF, Schema.CreativeWork);
		graph.edge(Schema.WebPage, RDFS.SUBCLASSOF, Schema.CreativeWork);
		graph.edge(Schema.CreativeWork, RDFS.SUBCLASSOF, Schema.Thing);
		
		OwlReasoner owl = new OwlReasoner(graph);
		
		Resource actual = owl.leastCommonSuperClass(Schema.VideoObject, Schema.WebPage);
		assertEquals(Schema.CreativeWork, actual);
	}
	
	@Test
	public void testDisjointTypes() throws Exception {
		Graph graph = new MemoryGraph();
		graph.edge(Schema.Person, RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.VideoObject, RDFS.SUBCLASSOF, Schema.MediaObject);
		graph.edge(Schema.AudioObject, RDFS.SUBCLASSOF, Schema.MediaObject);
		graph.edge(Schema.MediaObject, RDFS.SUBCLASSOF, Schema.CreativeWork);
		graph.edge(Schema.WebPage, RDFS.SUBCLASSOF, Schema.CreativeWork);
		
		Set<URI> set = new HashSet<>();
		set.add(Schema.Person);
		set.add(Schema.CreativeWork);
		set.add(Schema.MediaObject);
		set.add(Schema.VideoObject);
		set.add(Schema.AudioObject);
		set.add(Schema.WebPage);
		
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		Set<URI> actual = reasoner.disjointTypes(set);
		
		assertEquals(4, actual.size());
		assertTrue(actual.contains(Schema.Person));
		assertTrue(actual.contains(Schema.VideoObject));
		assertTrue(actual.contains(Schema.AudioObject));
		assertTrue(actual.contains(Schema.WebPage));
	}

	@Test
	public void test() throws AmbiguousPreferredClassException {
		
		Graph graph = new MemoryGraph();
		
		URI foafOrg = uri("http://xmlns.com/foaf/0.1/Organization");
		URI w3cOrg = ORG.Organization;
		URI schemaOrg = Schema.Organization;

		graph.edge(foafOrg, RDF.TYPE, OWL.CLASS);
		graph.edge(w3cOrg, RDF.TYPE, OWL.CLASS);
		graph.edge(schemaOrg, RDF.TYPE, RDFS.CLASS);
		graph.edge(schemaOrg, RDF.TYPE, Konig.PreferredClass);
		graph.edge(Schema.Person, RDF.TYPE, OWL.CLASS);
		graph.edge(foafOrg, OWL.EQUIVALENTCLASS, w3cOrg);
		graph.edge(w3cOrg, OWL.EQUIVALENTCLASS, schemaOrg);
		
		OwlReasoner owl = new OwlReasoner(graph);
		
		assertEquals(schemaOrg, owl.preferredClass(schemaOrg).getId());
		assertEquals(schemaOrg, owl.preferredClass(foafOrg).getId());
		assertEquals(schemaOrg, owl.preferredClass(w3cOrg).getId());
		
		Set<Vertex> set = owl.equivalentClasses(w3cOrg);
		assertEquals(3, set.size());
		
		assertEquals(Schema.Person, owl.preferredClass(Schema.Person).getId());
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

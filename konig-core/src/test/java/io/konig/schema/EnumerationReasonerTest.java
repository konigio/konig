package io.konig.schema;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.NamespaceInfo;
import io.konig.core.NamespaceInfoManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.core.vocab.Schema;

public class EnumerationReasonerTest {

	@Test
	public void testNotEnumNamespace() {
		
		Graph graph = new MemoryGraph();
		
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		
		graph.edge(Schema.Person, RDF.TYPE, OWL.CLASS);
		
		NamespaceInfoManager nim = new NamespaceInfoManager();
		
		nim.load(graph);
		
		EnumerationReasoner reasoner = new EnumerationReasoner();
		
		reasoner.annotateEnumerationNamespaces(graph, nim);
		
		NamespaceInfo info = nim.getNamespaceInfo(Schema.NAMESPACE);
		
		assertTrue(!info.getType().contains(Konig.EnumNamespace));
	}
	

	@Test
	public void testSubclass() {

		Graph graph = new MemoryGraph();
		
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		
		NamespaceInfoManager nim = new NamespaceInfoManager();
		
		nim.load(graph);
		
		EnumerationReasoner reasoner = new EnumerationReasoner();
		
		reasoner.annotateEnumerationNamespaces(graph, nim);
		
		NamespaceInfo info = nim.getNamespaceInfo(Schema.NAMESPACE);
		
		assertTrue(info.getType().contains(Konig.EnumNamespace));
	}
	

	@Test
	public void testEnumMember() {


		Graph graph = new MemoryGraph();
		
		graph.edge(Konig.Day, RDF.TYPE, Konig.TimeUnit);
		graph.edge(Konig.TimeUnit, RDFS.SUBCLASSOF, Schema.Enumeration);
		
		NamespaceInfoManager nim = new NamespaceInfoManager();
		
		nim.load(graph);
		
		EnumerationReasoner reasoner = new EnumerationReasoner();
		
		reasoner.annotateEnumerationNamespaces(graph, nim);
		
		NamespaceInfo info = nim.getNamespaceInfo(Konig.NAMESPACE);
		
		assertTrue(info.getType().contains(Konig.EnumNamespace));
	}
	

	@Test
	public void testNamedIndividual() {

		Graph graph = new MemoryGraph();
		
		graph.edge(Konig.TimeUnit, RDF.TYPE, OwlVocab.NamedIndividual);
		
		NamespaceInfoManager nim = new NamespaceInfoManager();
		
		nim.load(graph);
		
		EnumerationReasoner reasoner = new EnumerationReasoner();
		
		reasoner.annotateEnumerationNamespaces(graph, nim);
		
		NamespaceInfo info = nim.getNamespaceInfo(Konig.NAMESPACE);
		
		assertTrue(info.getType().contains(Konig.EnumNamespace));
	}


}

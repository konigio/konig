package io.konig.schema;

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

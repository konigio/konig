package io.konig.triplestore.gae;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;

import io.konig.triplestore.core.Triplestore;

public abstract class AbstractGaeTriplestoreTest extends DatastoreTest {
	
	abstract Triplestore createTriplestore();
	
	@Test
	public void testNamespaces() throws Exception {
		
		Namespace schema = new NamespaceImpl("schema", "http://schema.org");
		Namespace owl = new NamespaceImpl("owl", OWL.NAMESPACE);
		
		List<Namespace> list = new ArrayList<>();
		
		list.add(schema);
		list.add(owl);

		Triplestore store = createTriplestore();
		store.putNamespaces(list);
		
		List<String> names = new ArrayList<>();
		names.add("http://schema.org");
		names.add(OWL.NAMESPACE);
		Collection<Namespace> resultSet = store.getNamespacesByName(names);
		
		assertNamespace(resultSet, schema);
		assertNamespace(resultSet, owl);
		
		List<String> prefixes = new ArrayList<>();
		prefixes.add("schema");
		prefixes.add("owl");
		resultSet = store.getNamespacesByPrefix(prefixes);

		assertNamespace(resultSet, schema);
		assertNamespace(resultSet, owl);
	}

	private void assertNamespace(Collection<Namespace> resultSet, Namespace ns) {
		
		for (Namespace a : resultSet) {
			if (a.getPrefix().equals(ns.getPrefix()) && a.getName().equals(ns.getName())) {
				return;
			}
		}
		fail("Namespace not found: " + ns.getPrefix());
		
	}

	@Test
	public void test() throws Exception {
		
		LinkedHashModel aliceProperties = new LinkedHashModel();
		
		URI alice = uri("http://example.com/alice");
		URI bob = uri("http://example.com/bob");
		URI cathy = uri("http://example.com/cathy");
		Literal aliceName = literal("Alice");
		
		aliceProperties.add(alice, Schema.knows, bob);
		aliceProperties.add(alice, Schema.name, aliceName);
		
		Triplestore store = createTriplestore();
		
		store.putResource(alice, aliceProperties);
		Collection<Statement> outEdges = store.getStatements(alice, null, null);
		
		LinkedHashModel cathyProperties = new LinkedHashModel();
		cathyProperties.add(cathy, Schema.knows, bob);
		
		store.putResource(cathy, cathyProperties);
		
		LinkedHashModel loaded = new LinkedHashModel(outEdges);
		
		assertTrue(loaded.contains(alice, Schema.knows, bob));
		assertTrue(loaded.contains(alice, Schema.name, aliceName));
		
		Collection<Statement> inEdges = store.getStatements(null, null, bob);
		assertEquals(2, inEdges.size());
		
		LinkedHashModel bobInEdges = new LinkedHashModel(inEdges);
		
		assertTrue(bobInEdges.contains(alice, Schema.knows, bob));
		assertTrue(bobInEdges.contains(cathy, Schema.knows, bob));
		
		store.remove(alice);
		
		Collection<Statement> aliceOut = store.getStatements(alice, null, null);
		assertTrue(aliceOut.isEmpty());
		
		Collection<Statement> aliceIn = store.getStatements(null, null, alice);
		assertTrue(aliceIn.isEmpty());
		
	}

	private Literal literal(String value) {
		return new LiteralImpl(value);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

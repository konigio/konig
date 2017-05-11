package io.konig.triplestore.gae;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;

import io.konig.triplestore.core.Triplestore;

public abstract class AbstractGaeTriplestoreTest extends DatastoreTest {
	
	abstract Triplestore createTriplestore();

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
		
		store.save(alice, aliceProperties);
		Collection<Statement> outEdges = store.getOutEdges(alice);
		
		LinkedHashModel cathyProperties = new LinkedHashModel();
		cathyProperties.add(cathy, Schema.knows, bob);
		
		store.save(cathy, cathyProperties);
		
		LinkedHashModel loaded = new LinkedHashModel(outEdges);
		
		assertTrue(loaded.contains(alice, Schema.knows, bob));
		assertTrue(loaded.contains(alice, Schema.name, aliceName));
		
		Collection<Statement> inEdges = store.getInEdges(bob);
		assertEquals(2, inEdges.size());
		
		LinkedHashModel bobInEdges = new LinkedHashModel(inEdges);
		
		assertTrue(bobInEdges.contains(alice, Schema.knows, bob));
		assertTrue(bobInEdges.contains(cathy, Schema.knows, bob));
		
		store.remove(alice);
		
		Collection<Statement> aliceOut = store.getOutEdges(alice);
		assertTrue(aliceOut.isEmpty());
		
		Collection<Statement> aliceIn = store.getInEdges(alice);
		assertTrue(aliceIn.isEmpty());
		
	}

	private Literal literal(String value) {
		return new LiteralImpl(value);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

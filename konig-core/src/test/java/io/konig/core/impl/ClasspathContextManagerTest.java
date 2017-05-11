package io.konig.core.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

import org.junit.Test;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.vocab.Schema;

public class ClasspathContextManagerTest {

	@Test
	public void test() {
		
		ClasspathContextManager contextManager = new ClasspathContextManager();
		
		Context context = contextManager.getContextByURI("http://example.com/ctx/foo");
		assertTrue(context != null);
		Term term = context.getTerm("Person");
		assertTrue(term != null);
		
		assertEquals(term.getExpandedId(), Schema.Person);
	}

}

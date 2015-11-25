package io.konig.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class ContextTest {

	@Test
	public void testExpandIRI() {
		
		Context context = new Context("");
		
		context.addTerm("schema", "http://schema.org/");
		context.addTerm("Person", "schema:Person");
		
		String person = context.expandIRI("Person");
		
		assertEquals("http://schema.org/Person", person);
		
	}

}

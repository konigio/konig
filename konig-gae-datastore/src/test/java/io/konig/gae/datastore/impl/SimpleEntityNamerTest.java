package io.konig.gae.datastore.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.konig.core.vocab.Schema;

public class SimpleEntityNamerTest {

	@Test
	public void test() {
		
		SimpleEntityNamer namer = new SimpleEntityNamer();
		String name = namer.entityName(Schema.Person);
		
		assertEquals("Person", name);
		
		
	}

}

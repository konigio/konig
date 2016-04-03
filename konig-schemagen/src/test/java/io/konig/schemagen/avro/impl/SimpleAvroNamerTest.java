package io.konig.schemagen.avro.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleAvroNamerTest {

	@Test
	public void test() {
		SimpleAvroNamer namer = new SimpleAvroNamer();
		
		String actual = namer.toAvroNamespace("http://www.konig.io/resources/");
		
		assertEquals("io.konig.resources", actual);
	}

}

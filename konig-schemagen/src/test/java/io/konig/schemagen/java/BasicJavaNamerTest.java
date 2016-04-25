package io.konig.schemagen.java;

import static org.junit.Assert.*;

import org.junit.Test;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;

public class BasicJavaNamerTest {

	@Test
	public void test() {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		
		BasicJavaNamer namer = new BasicJavaNamer("com.example.", nsManager);
		
		String value = namer.javaClassName(Schema.Person);
		assertEquals("com.example.schema.Person", value);
		
	}

}

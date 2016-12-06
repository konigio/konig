package io.konig.schemagen.java;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;

public class BasicJavaNamerTest {
	
	@Test
	public void testWriterName() {
		URI shapeId = uri("http://example.com/shapes/v1/schema/Person");
		
		String basePackage = "com.acme.base";
		BasicJavaNamer namer = new BasicJavaNamer(basePackage, null);
		
		String className = namer.writerName(shapeId, Format.JSON);
		assertEquals("com.acme.base.io.shapes.v1.schema.PersonJsonWriter", className);
	}

	@Test
	public void test() {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		
		BasicJavaNamer namer = new BasicJavaNamer("com.example.", nsManager);
		
		String value = namer.javaClassName(Schema.Person);
		assertEquals("com.example.impl.schema.PersonImpl", value);
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

package io.konig.schemagen.merge;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;

public class SimpleShapeNamerTest {

	@Test
	public void test() {
		
		MemoryNamespaceManager manager = new MemoryNamespaceManager();
		manager.add("schema", "http://schema.org/");
		
		SimpleShapeNamer namer = new SimpleShapeNamer(manager, "http://example.com/shape/dw/");
		URI shapeName = namer.shapeName(Schema.Person);
		
		assertEquals("http://example.com/shape/dw/schema/Person", shapeName.stringValue());
	}

}

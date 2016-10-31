package io.konig.shacl.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.vocab.Schema;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ClasspathShapeManagerTest {

	@Test
	public void test() {
		
		ClasspathShapeManager manager = ClasspathShapeManager.instance();
		
		
		URI personShapeId = uri("http://example.com/shape/Person");
		
		Shape shape = manager.getShapeById(personShapeId);
		
		assertTrue(shape != null);
		
		PropertyConstraint p = shape.getPropertyConstraint(Schema.givenName);
		assertTrue(p != null);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

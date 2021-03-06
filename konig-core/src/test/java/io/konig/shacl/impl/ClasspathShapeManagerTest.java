package io.konig.shacl.impl;

/*
 * #%L
 * Konig SHACL
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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

package io.konig.schemagen.java;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema1", "http://example.com/shapes/v1/schema/");
		
		String basePackage = "com.acme.base";
		BasicJavaNamer namer = new BasicJavaNamer(basePackage, nsManager);
		
		String className = namer.writerName(shapeId, Format.JSON);
		assertEquals("com.acme.base.io.writer.schema1.PersonJsonWriter", className);
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

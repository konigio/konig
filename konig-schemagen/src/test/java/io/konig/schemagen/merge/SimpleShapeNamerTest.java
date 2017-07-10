package io.konig.schemagen.merge;

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


import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.shacl.SimpleShapeNamer;

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

package io.konig.shacl.impl;

/*
 * #%L
 * konig-shacl
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


import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.shacl.Shape;

public class SimpleShapeMediaTypeNamerTest {

	@Test
	public void test() {
		
		URI shapeId = uri("http://www.konig.io/shapes/v1/schema/Person");
		Shape shape = new Shape(shapeId);
		
		SimpleShapeMediaTypeNamer namer = new SimpleShapeMediaTypeNamer();
		
		String name = namer.baseMediaTypeName(shape);
		
		assertEquals("application/vnd.konig.v1.schema.person", name);
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

import io.konig.core.vocab.Schema;

public class DefaultTabularShapeNamerTest {
	
	private DefaultTabularShapeNamer namer = new DefaultTabularShapeNamer();

	@Test
	public void testEducationalOrganizationName() {
		URI subjectShapeId = uri("http://example.com/shapes/EducationalOrganizationShape");
		URI actual = namer.reifiedPropertyShapeId(subjectShapeId, Schema.name);
		
		assertEquals("http://example.com/shapes/EDUCATIONAL_ORGANIZATION_NAME_SHAPE", actual.stringValue());
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

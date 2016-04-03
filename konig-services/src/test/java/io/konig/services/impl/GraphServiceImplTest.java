package io.konig.services.impl;

/*
 * #%L
 * Konig Services
 * %%
 * Copyright (C) 2015 Gregory McFall
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
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Traversal;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.services.GraphService;
import io.konig.services.KonigServiceTest;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class GraphServiceImplTest extends KonigServiceTest {

	@Test
	public void testPerson() throws Exception {
		
		
		GraphService service = config().getGraphService();
		
		MemoryGraph graph = new MemoryGraph();
		
		URI alice = uri("http://example.com/alice");
		
		ShapeBuilder builder = new ShapeBuilder("http://www.konig.io/shape/schema/Person-v1");
		
		builder.property(Schema.givenName)
			.datatype(XMLSchema.STRING)
			.maxCount(1)
			.property(Schema.familyName)
			.maxCount(1);
		
		Shape personShape = builder.shape();
		
		
		graph.v(alice)
			.addLiteral(Schema.givenName, "Alice")
			.addLiteral(Schema.familyName, "Smith");
		
		
		service.put(alice, personShape, graph);
		
		MemoryGraph sink = new MemoryGraph();
		service.get(alice, sink);
		
		Traversal aliceT = sink.v(alice);
		
		assertTrue(aliceT.hasValue(Schema.givenName, "Alice").size() == 1);
		assertTrue(aliceT.hasValue(Schema.familyName, "Smith").size() == 1);
		
		
	}

}

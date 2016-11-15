package io.konig.shacl;

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


import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.path.PathFactory;
import io.konig.core.vocab.Schema;

public class ShapeReasonerTest {

	@Test
	public void test() {

		SimpleLocalNameService localNameService = new SimpleLocalNameService();
		localNameService.add("parent", Schema.parent);
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);

		URI aliceId = uri("http://example.com/person/alice");
		URI bobId = uri("http://example.com/person/bob");
		URI cathyId = uri("http://example.com/person/cathy");
		URI donId = uri("http://example.com/person/don");
		
		URI shapeId = uri("http://example.com/shapes/v1/schema/Person");
		URI grandparent = uri("http://example.com/ns/grandparent");
		
		
		MemoryGraph data = new MemoryGraph();
		data.builder()
			.beginSubject(aliceId)
				.addProperty(Schema.parent, bobId)
			.endSubject()
			.beginSubject(bobId)
				.addProperty(Schema.parent, cathyId)
				.addProperty(Schema.parent, donId)
			.endSubject();
			
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(shapeId)
			.beginProperty(grandparent)
				.equivalentPath("/parent/parent")
			.endProperty()
		.endShape();
			

		PathFactory factory = new PathFactory(nsManager, localNameService);
		
		ShapeReasoner reasoner = new ShapeReasoner(factory);
		Vertex alice = data.getVertex(aliceId);
		Shape shape = builder.getShape(shapeId);
		
		reasoner.assertEquivalentPaths(alice, shape);
		
		assertTrue(data.contains(aliceId, grandparent, cathyId));
		assertTrue(data.contains(aliceId, grandparent, donId));
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}
}

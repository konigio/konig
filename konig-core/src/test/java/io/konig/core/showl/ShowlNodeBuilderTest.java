package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class ShowlNodeBuilderTest {
	
	private Graph graph = new MemoryGraph(MemoryNamespaceManager.getDefaultInstance());
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private ShowlServiceImpl showlService = new ShowlServiceImpl(reasoner);
	private ShowlNodeShapeBuilder nodeBuilder = new ShowlNodeShapeBuilder(showlService, showlService);

	@Test
	public void test() {
		
		URI shapeId = uri("http://example.com/shape/PersonShape");
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		shapeBuilder
			.beginShape(shapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
					.datatype(XMLSchema.STRING)
					.minCount(0)
					.maxCount(1)
				.endProperty()

				.beginProperty(Schema.familyName)
					.datatype(XMLSchema.STRING)
					.minCount(0)
					.maxCount(1)
				.endProperty()

				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.minCount(0)
					.maxCount(1)
					.formula("CONCAT($.givenName, ' ', $.familyName)", Schema.givenName, Schema.familyName)
				.endProperty()
			.endShape();
		
		Shape shape = shapeBuilder.getShape(shapeId);
		
		ShowlNodeShape node = nodeBuilder.buildNodeShape(null, shape);
		
		assertTrue(node != null);
	}

	private URI uri(String stringValue) {
		return new URIImpl(stringValue);
	}

}

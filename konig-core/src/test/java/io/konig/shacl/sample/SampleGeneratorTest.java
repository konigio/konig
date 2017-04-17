package io.konig.shacl.sample;

/*
 * #%L
 * Konig Core
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

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Context;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.BasicContext;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.DC;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class SampleGeneratorTest {
	private Graph graph = new MemoryGraph(new MemoryNamespaceManager());
	private ShapeManager shapeManager = new MemoryShapeManager();
	private SampleGenerator generator = new SampleGenerator();
	
	@Ignore
	public void testIriTemplate() throws Exception {
		ShapeBuilder builder = new ShapeBuilder();
		
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		builder.beginShape(shapeId)
			.beginProperty(DC.identifier)
				.datatype(XMLSchema.STRING)
				.maxCount(1)
			.endProperty();
		
		Shape shape = builder.getShape(shapeId);
		Context context = new BasicContext(null);
		context.addTerm("identifier", DC.identifier.stringValue());
		shape.setIriTemplate(new IriTemplate(context,"http://example.com/person/{identifier}"));
		
		Vertex v = generator.generate(shape, graph);
		
		Value identifier = v.getValue(DC.identifier);
		String expected = "http://example.com/person/" + identifier.stringValue();
		
		assertEquals(expected, v.getId().stringValue());
		
		
		
	}

	@Ignore
	public void testKitchenSink() throws Exception {
		load("SampleGeneratorTest/rdf");
		
		URI shapeId = uri("http://example.com/ns/test/KitchenSinkShape");
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape != null);
		
		Vertex v = generator.generate(shape, graph);
		
		assertTrue(v.getId() instanceof URI);
		
		System.out.println(v);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void load(String resource) throws Exception {
		File dir = new File( "src/test/resources/" + resource );
		RdfUtil.loadTurtle(dir, graph, shapeManager);
		
	}

}

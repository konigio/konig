package io.konig.shacl;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.vocab.Schema;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class ClassHierarchyTest {
	private NamespaceManager nsManager;
	private Graph graph;
	private ShapeManager shapeManager;
	private OwlReasoner owlReasoner;
	private ClassHierarchy hierarchy;
	
	@Before
	public void setUp() {
		nsManager = new MemoryNamespaceManager();
		graph = new MemoryGraph(nsManager);
		shapeManager = new MemoryShapeManager();
		owlReasoner = new OwlReasoner(graph);
	}
	@Test
	public void test() throws Exception {
		load("ClassHierarchyTest/model.ttl");

		SimpleValueFormat iriTemplate = new SimpleValueFormat("http://example.com/shapes/canonical/{targetClassNamespacePrefix}/{targetClassLocalName}");
		hierarchy = new ClassHierarchy(iriTemplate, shapeManager, owlReasoner);
		Shape workShape = hierarchy.getShapeForClass(Schema.CreativeWork);
		assertTrue(workShape != null);

		List<PropertyConstraint> workProperties = workShape.getProperty();
		assertEquals(3, workProperties.size());
		
		OrConstraint orList = workShape.getOr();
		assertTrue(orList != null);
		
		assertEquals(2, orList.getShapes().size());
		
		Shape mediaShape = orList.findShapeByTargetClass(Schema.MediaObject);
		assertTrue(mediaShape != null);
		
		List<PropertyConstraint> mediaProperties = mediaShape.getProperty();
		assertEquals(3, mediaProperties.size());
		
		AndConstraint andList = workShape.getAnd();
		assertTrue(andList != null);
		
		assertEquals(1, andList.getShapes().size());
		
		Shape thing = andList.getShapes().get(0);
		PropertyConstraint name = thing.getPropertyConstraint(Schema.name);
		assertTrue(name == null);
		
	}
	

	private void load(String resource) throws RDFParseException, RDFHandlerException, IOException {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		
		RdfUtil.loadTurtle(graph, input, "");
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
		
		
	}

}

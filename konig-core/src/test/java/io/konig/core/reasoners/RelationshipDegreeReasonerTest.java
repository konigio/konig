package io.konig.core.reasoners;

/*
 * #%L
 * Konig Core
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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.impl.SimpleLocalNameService;
import io.konig.core.path.NamespaceMapAdapter;
import io.konig.core.pojo.impl.PropertyInfo;
import io.konig.formula.FormulaParser;
import io.konig.formula.ShapePropertyOracle;
import io.konig.rio.turtle.NamespaceMap;
import io.konig.shacl.RelationshipDegree;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import junitx.util.PrivateAccessor;


public class RelationshipDegreeReasonerTest{
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private MemoryShapeManager shapeManager = new MemoryShapeManager();
	
	@Test
	public void testGetShMaxCardinality() throws Throwable {
		load("src/test/resources/relationship-degree-reasoner");
		final Class clazz = Class.forName("io.konig.core.reasoners.RelationshipDegreeReasoner$Worker");
		final Constructor constructor = clazz.getDeclaredConstructor(Graph.class,ShapeManager.class,Boolean.class);
		constructor.setAccessible(true);
		final Object instance = constructor.newInstance(graph,shapeManager,false);
		final Integer result = (Integer) PrivateAccessor.invoke(instance, "getShMaxCardinality", new Class[]{URI.class}, new Object[]{uri("http://example.com/ns/alias/address")});
		assertEquals(3, result.intValue());
		
	}
	@Test
	public void testGetOwlMaxCardinality() throws Throwable{
		load("src/test/resources/relationship-degree-reasoner");
		final Class clazz = Class.forName("io.konig.core.reasoners.RelationshipDegreeReasoner$Worker");
		final Constructor constructor = clazz.getDeclaredConstructor(Graph.class,ShapeManager.class,Boolean.class);
		constructor.setAccessible(true);
		final Object instance = constructor.newInstance(graph,shapeManager,false);
		Integer owlMaxCardinality = (Integer) PrivateAccessor.invoke(instance, "getOwlMaxCardinality", new Class[]{URI.class}, new Object[]{uri("http://schema.org/givenName")});
		assertTrue(owlMaxCardinality==1);
		owlMaxCardinality = (Integer) PrivateAccessor.invoke(instance, "getOwlMaxCardinality", new Class[]{URI.class}, new Object[]{uri("http://example.com/ns/alias/address")});
		assertTrue(owlMaxCardinality==5);
		owlMaxCardinality = (Integer) PrivateAccessor.invoke(instance, "getOwlMaxCardinality", new Class[]{URI.class}, new Object[]{uri("http://schema.org/owns")});
		assertTrue(owlMaxCardinality==3);
	}
	
	
	@Test
	public void testGetRelationshipDegree() throws Exception{
		load("src/test/resources/relationship-degree-reasoner");
		URI shapeId = uri("http://example.com/shapes/PersonShape");

		Shape shape = shapeManager.getShapeById(shapeId);
		RelationshipDegreeReasoner reasoner=new RelationshipDegreeReasoner();
		reasoner.computeRelationshipDegree(graph, shapeManager, false);
		shape=shapeManager.getShapeById(shapeId);
		RelationshipDegree relationshipDegree=shape.getPropertyConstraint(uri("http://schema.org/owns")).getRelationshipDegree();
		assertTrue(RelationshipDegree.OneToMany.equals(relationshipDegree));
		relationshipDegree=shape.getPropertyConstraint(uri("http://example.com/ns/alias/address")).getRelationshipDegree();
		assertTrue(RelationshipDegree.ManyToMany.equals(relationshipDegree));
		relationshipDegree=shape.getPropertyConstraint(uri("http://schema.org/birthPlace")).getRelationshipDegree();
		assertTrue(RelationshipDegree.OneToOne.equals(relationshipDegree));
		relationshipDegree=shape.getPropertyConstraint(uri("http://schema.org/knowsAbout")).getRelationshipDegree();
		assertTrue(RelationshipDegree.ManyToOne.equals(relationshipDegree));
		relationshipDegree=shape.getPropertyConstraint(uri("http://schema.org/givenName")).getRelationshipDegree();
		assertTrue(RelationshipDegree.ManyToOne.equals(relationshipDegree));
	}
	
	private void load(String path) throws RDFParseException, RDFHandlerException, IOException {		
		File sourceDir = new File(path);
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		SimpleLocalNameService nameService = new SimpleLocalNameService();
		nameService.addAll(graph);
		NamespaceMap nsMap = new NamespaceMapAdapter(graph.getNamespaceManager());
		ShapePropertyOracle oracle = new ShapePropertyOracle();
		FormulaParser parser = new FormulaParser(oracle, nameService, nsMap);
	}

	private URI uri(String text) {
		return new URIImpl(text);
	}
}

package io.konig.shacl.io;

/*
 * #%L
 * Konig Core
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.datasource.GoogleBigQueryTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class ShapeLoaderTest {

	
	@Test
	public void testShapeDataSource() {
		URI shapeId = uri("http://example.com/PersonShape");
		URI bucketId = uri("gs://com.example.person");
		String iriTemplateValue = "http://example.com/users/{user_id}";
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(shapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addLiteral(Konig.iriTemplate, iriTemplateValue)
				.beginBNode(Konig.shapeDataSource)
					.addProperty(RDF.TYPE, Konig.GoogleBigQueryTable)
					.addLiteral(DCTERMS.IDENTIFIER, "acme.Person")
					.addProperty(Konig.bigQuerySource, bucketId)
				.endSubject()
			.endSubject();
		
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		assertTrue(shape.getIriTemplate()!=null);
		assertEquals(iriTemplateValue, shape.getIriTemplate().toString());
		
		List<DataSource> dataSourceList = shape.getShapeDataSource();
		assertTrue(dataSourceList != null);
		assertEquals(1, dataSourceList.size());
		DataSource dataSource = dataSourceList.get(0);
		DataSource source = dataSourceList.get(0);
		assertTrue(dataSource instanceof GoogleBigQueryTable);
		assertEquals("acme.Person", dataSource.getIdentifier());
		GoogleBigQueryTable table = (GoogleBigQueryTable) source;
		
		Set<DataSource> set = table.getBigQuerySource();
		assertTrue(set != null && set.size()==1);
		
		DataSource bucket = set.iterator().next();
		assertEquals(bucketId, bucket.getId());
	
	}
	
	@Test
	public void testType() {
		
		URI shapeId = uri("http://example.com/PersonShape");
		URI OriginShape = uri("http://example.com/ns/OriginShape");
		
		Graph graph = new MemoryGraph();
		
		graph.edge(shapeId, RDF.TYPE, SH.Shape);
		graph.edge(shapeId, RDF.TYPE, OriginShape);
		
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(shapeManager);
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		
		List<URI> typeList = shape.getType();
		assertTrue(typeList != null);
		assertEquals(2, typeList.size());
		assertEquals(SH.Shape, typeList.get(0));
		assertEquals(OriginShape, typeList.get(1));
		
		
	}

	@Test
	public void testShape() {
		URI shapeId = uri("http://example.com/shape/PersonShape");
		URI addressShapeId = uri("http://example.com/shape/PostalAddress");
		
		Graph graph = new MemoryGraph();
		
		graph.builder()
			.beginSubject(shapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(SH.targetClass, Schema.Person)
				.beginBNode(SH.property)
					.addProperty(SH.predicate, Schema.address)
					.addProperty(SH.shape, addressShapeId)
				.endSubject()
			.endSubject()
			.beginSubject(addressShapeId)
				.addProperty(SH.targetClass, Schema.PostalAddress)
			.endSubject();
		
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(shapeManager);
		
		loader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		
		PropertyConstraint constraint = shape.getPropertyConstraint(Schema.address);
		assertTrue(constraint!=null);
		
		Shape addressShape = constraint.getShape();
		assertTrue(addressShape!=null);
		
		assertEquals(addressShape.getTargetClass(), Schema.PostalAddress);
		
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

package io.konig.gcp.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
 * #%L
 * Konig Google Cloud Platform Model
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


import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class ShapeLoaderTest {

	private ShapeManager shapeManager = new MemoryShapeManager();
	private ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
	
	@Before
	public void setUp() {
		GcpShapeConfig.init();
	}
	
	@Test
	public void testOverlaySortProperty() {

		MemoryGraph graph = new MemoryGraph();
		
		URI sortPropertyValue = uri("http://example.com/ns/core/lastModified");
		
		URI shapeId = uri("http://example.com/PersonShape");
		
		URI tableId = uri("http://example.com/table/Person");
		
		URI stageId = uri("http://example.com/sys/WarehouseStaging");
		
		graph.builder()
			.beginSubject(shapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(Konig.shapeDataSource, tableId)
			.endSubject()
			.beginSubject(tableId)
				.addProperty(RDF.TYPE, Konig.GoogleCloudSqlTable)
				.addProperty(Schema.isPartOf, stageId)
				.addProperty(Konig.overlaySortProperty, sortPropertyValue)
			.endSubject()
			;
		
		
		
		
		shapeLoader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		List<DataSource> list = shape.getShapeDataSource();
		assertEquals(1, list.size());
		
		DataSource ds = list.get(0);
		URI sortProperty = ds.getOverlaySortProperty();
		assertTrue(sortProperty!=null);
		assertEquals(sortPropertyValue.stringValue(), sortProperty.stringValue());
	}
	
	@Test 
	public void testIsPartOf() {
		MemoryGraph graph = new MemoryGraph();
		
		URI shapeId = uri("http://example.com/PersonShape");
		
		URI tableId = uri("http://example.com/table/Person");
		
		URI stageId = uri("http://example.com/sys/WarehouseStaging");
		
		graph.builder()
			.beginSubject(shapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(Konig.shapeDataSource, tableId)
			.endSubject()
			.beginSubject(tableId)
				.addProperty(RDF.TYPE, Konig.GoogleCloudSqlTable)
				.addProperty(Schema.isPartOf, stageId)
			.endSubject()
			;
		
		
		
		
		shapeLoader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		List<DataSource> list = shape.getShapeDataSource();
		assertEquals(1, list.size());
		
		DataSource ds = list.get(0);
		assertTrue(ds instanceof GoogleCloudSqlTable);
		
		GoogleCloudSqlTable table = (GoogleCloudSqlTable) ds;
		assertEquals(1, table.getIsPartOf().size());
		assertEquals(stageId, table.getIsPartOf().get(0));
	}
	
	@Test
	public void testGoogleCloudSqlTable() {
		MemoryGraph graph = new MemoryGraph();
		
		URI shapeId = uri("http://example.com/PersonShape");
		
		URI tableId = uri("https://www.googleapis.com/sql/v1beta4/projects/{gcpProjectId}/instances/schema/databases/schema/tables/PersonShape");
		
		graph.builder()
			.beginSubject(shapeId)
				.addProperty(RDF.TYPE, SH.Shape)
				.addProperty(Konig.shapeDataSource, tableId)
			.endSubject()
			.beginSubject(tableId)
				.addProperty(RDF.TYPE, Konig.GoogleCloudSqlTable)
			.endSubject();
		
		
		
		shapeLoader.load(graph);
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		List<DataSource> list = shape.getShapeDataSource();
		assertEquals(1, list.size());
		
		DataSource ds = list.get(0);
		assertTrue(ds instanceof GoogleCloudSqlTable);
		
		GoogleCloudSqlTable table = (GoogleCloudSqlTable) ds;
		assertEquals("PersonShape", table.getTableName());
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.google.api.services.bigquery.model.CsvOptions;
import com.google.api.services.bigquery.model.ExternalDataConfiguration;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class GcpShapeLoaderTest {
	
	@Before
	public void setUp() {
		GcpShapeConfig.init();
	}

	@Test
	public void testBigQueryCsvBucket() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/testBigQueryCsvBucket.ttl");

		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		URI shapeId = uri("http://example.com/shapes/PersonLiteShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape != null);
		
		List<DataSource> list = shape.getShapeDataSource();
		assertTrue(list != null);
		assertEquals(1, list.size());
		
		GoogleBigQueryTable table = (GoogleBigQueryTable) list.get(0);
		ExternalDataConfiguration config = table.getExternalDataConfiguration();
		assertTrue(config != null);
		
		List<String> sourceUriList = config.getSourceUris();
		assertTrue(sourceUriList != null);
		assertEquals(1, sourceUriList.size());
		assertEquals("gs://person.staging.edw.pearson.com/*", sourceUriList.get(0));
		
		CsvOptions options = config.getCsvOptions();
		assertTrue(options != null);
		assertEquals(new Long(1), options.getSkipLeadingRows());
		assertEquals("CSV", config.getSourceFormat());
	}
	
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
		assertEquals("<http://example.com/users/{user_id}>", shape.getIriTemplate().toString());
		
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
	public void testCloudStorageBucket() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/testCloudStorageBucket.ttl");

		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		URI shapeId = uri("http://example.com/shapes/PersonLiteShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape != null);
		
		List<DataSource> list = shape.getShapeDataSource();
		assertTrue(list != null);
		assertEquals(1, list.size());
		
		assertTrue(list.get(0) instanceof GoogleCloudStorageBucket);
		GoogleCloudStorageBucket bucket = (GoogleCloudStorageBucket) list.get(0);
		
		
		
		assertEquals("{gcpProjectId}", bucket.getProjectId());
		assertEquals("multi_regional", bucket.getStorageClass());
		assertEquals("us", bucket.getLocation());
		assertEquals("person.staging.edw.pearson.com", bucket.getName());
	}
	

	@Test
	public void testBigQueryTableReference() throws Exception {
		Graph graph = loadGraph("ShapeLoaderTest/testBigQueryTableReference.ttl");

		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		URI shapeId = uri("http://example.com/shapes/PersonLiteShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape != null);
		
		List<DataSource> list = shape.getShapeDataSource();
		assertTrue(list != null);
		assertEquals(1, list.size());
		
		assertTrue(list.get(0) instanceof GoogleBigQueryTable);
		GoogleBigQueryTable table = (GoogleBigQueryTable) list.get(0);
		
		BigQueryTableReference ref = table.getTableReference();
		assertTrue(ref != null);
		
		assertEquals("{gcpProjectId}", ref.getProjectId());
		assertEquals("schema", ref.getDatasetId());
		assertEquals("Person", ref.getTableId());
	}
	
	private Graph loadGraph(String resource) throws RDFParseException, RDFHandlerException, IOException {
		Graph graph = new MemoryGraph();
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		try {
			RdfUtil.loadTurtle(graph, input, "");
		} finally {
			IOUtil.close(input, resource);
		}
		return graph;
	}

	
	
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

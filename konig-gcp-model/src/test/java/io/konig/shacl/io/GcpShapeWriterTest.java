package io.konig.shacl.io;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import com.google.api.services.bigquery.model.CsvOptions;
import com.google.api.services.bigquery.model.ExternalDataConfiguration;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.shacl.Shape;

public class GcpShapeWriterTest {
	
	@Test
	public void testBigQueryCsvBucket() throws Exception {

		URI shapeId = uri("http://example.com/PersonShape");
		URI dataSourceId = uri("http://example.com/datasource");
		
		Shape shape = new Shape(shapeId);
		GoogleBigQueryTable table = new GoogleBigQueryTable();
		table.setId(dataSourceId);
		
		ExternalDataConfiguration config = new ExternalDataConfiguration();
		table.setExternalDataConfiguration(config);
		List<String> sourceUris = new ArrayList<>();
		sourceUris.add("gs://foo.bar");
		config.setSourceFormat("CSV");
		config.setSourceUris(sourceUris);
		CsvOptions csvOptions = new CsvOptions();
		csvOptions.setSkipLeadingRows(1L);
		config.setCsvOptions(csvOptions);
		
		shape.addShapeDataSource(table);
		
		ShapeWriter shapeWriter = new ShapeWriter();
		
		Graph graph = new MemoryGraph();
		shapeWriter.emitShape(shape, graph);
		
		Vertex v = graph.getVertex(shapeId);
		Vertex dataSourceVertex = v.getVertex(Konig.shapeDataSource);
		Vertex configVertex = dataSourceVertex.getVertex(GCP.externalDataConfiguration);
		assertTrue(configVertex != null);
		
		assertLiteral(configVertex, GCP.sourceFormat, "CSV");
		assertLiteral(configVertex, GCP.sourceUris, "gs://foo.bar");
		
		Vertex optionsVertex = configVertex.getVertex(GCP.csvOptions);
		assertTrue(optionsVertex != null);
		
		assertLiteral(optionsVertex, GCP.skipLeadingRows, 1L);
	}
	
	
	@Test
	public void testCloudStorageBucket() throws Exception {

		URI shapeId = uri("http://example.com/PersonShape");
		URI dataSourceId = uri("gs://person.example.com");
		
		Shape shape = new Shape(shapeId);
		GoogleCloudStorageBucket bucket = new GoogleCloudStorageBucket();
		bucket.setId(dataSourceId);
		bucket.setName("person.example.com");
		bucket.setStorageClass("multi_regional");
		bucket.setLocation("us");
		bucket.setProjectId("myproject");
		
		shape.addShapeDataSource(bucket);
		
		ShapeWriter shapeWriter = new ShapeWriter();
		
		Graph graph = new MemoryGraph();
		shapeWriter.emitShape(shape, graph);
		
		Vertex v = graph.getVertex(shapeId);
		assertTrue(v!=null);
		
		Vertex datasource = v.getVertex(Konig.shapeDataSource);
		assertTrue(datasource!=null);
		
		assertLiteral(datasource, GCP.projectId, "myproject");
		assertLiteral(datasource, GCP.name, "person.example.com");
		assertLiteral(datasource, GCP.storageClass, "multi_regional");
		assertLiteral(datasource, GCP.location, "us");
		
	}
	
	@Test
	public void testShapeDataSource() throws Exception {
		
		URI shapeId = uri("http://example.com/PersonShape");
		URI dataSourceId = uri("urn:bigquery:acme.Person");
		
		String iriTemplateValue = "http://example.com/user/{user_id}";
		Shape shape = new Shape(shapeId);
		shape.setIriTemplate(new IriTemplate(iriTemplateValue));
		GoogleBigQueryTable table = new GoogleBigQueryTable();
		table.setId(dataSourceId);
		BigQueryTableReference tableRef = new BigQueryTableReference("myproject", "acme", "Person");
		table.setTableReference(tableRef);
		
		shape.addShapeDataSource(table);
		
		ShapeWriter shapeWriter = new ShapeWriter();
		
		Graph graph = new MemoryGraph();
		shapeWriter.emitShape(shape, graph);
		
	
		Vertex v = graph.getVertex(shapeId);
		assertTrue(v != null);
		assertEquals("<http://example.com/user/{user_id}>", v.getValue(Konig.iriTemplate).stringValue());
		
		Vertex w = v.getVertex(Konig.shapeDataSource);
		assertTrue(w!=null);


		assertEquals(Konig.GoogleBigQueryTable, w.getURI(RDF.TYPE));
		
		Vertex u = w.getVertex(GCP.tableReference);
		assertTrue(u != null);
		assertLiteral(u, GCP.projectId, "myproject");
		assertLiteral(u, GCP.datasetId, "acme");
		assertLiteral(u, GCP.tableId, "Person");
	}

	private void assertLiteral(Vertex u, URI predicate, String expected) {
		Value v = u.getValue(predicate);
		assertTrue("Failed to get value '" + predicate.getLocalName() + "'", v != null);
		assertEquals(expected, v.stringValue());
	
		
	}
	

	private void assertLiteral(Vertex u, URI predicate, long expected) {
		Value v = u.getValue(predicate);
		assertTrue("Failed to get value '" + predicate.getLocalName() + "'", v instanceof Literal);
	
		Literal literal = (Literal) v;
		assertEquals(expected, literal.longValue());
	
		
	}

	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

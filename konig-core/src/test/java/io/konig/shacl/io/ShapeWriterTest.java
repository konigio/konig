package io.konig.shacl.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

import java.io.File;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.activity.Activity;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.FileGetter;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.datasource.BigQueryTableReference;
import io.konig.datasource.GoogleBigQueryTable;
import io.konig.datasource.GoogleCloudStorageBucket;
import io.konig.formula.Expression;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;

public class ShapeWriterTest {
	
	@Test
	public void testFormula() throws Exception {

		URI shapeId = uri("http://example.com/IssueShape");
		URI completedPoints = uri("http://example.com/ns/completedPoints");
		Shape shape = new Shape(shapeId);
		PropertyConstraint p = new PropertyConstraint(completedPoints);
		shape.add(p);
		p.setFormula(new Expression("(status = ex:Complete) ? estimatedPoints : 0"));
		
		ShapeWriter shapeWriter = new ShapeWriter();
		Graph graph = new MemoryGraph();
		shapeWriter.emitShape(shape, graph);

		Vertex v = graph.getVertex(shapeId);
		assertTrue(v!=null);
		
		Vertex w = v.getVertex(SH.property);
		assertTrue(w != null);
		
		assertLiteral(w, Konig.formula, "(status = ex:Complete) ? estimatedPoints : 0");
		
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
		assertEquals(iriTemplateValue, v.getValue(Konig.iriTemplate).stringValue());
		
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

	@Test
	public void test() throws Exception {
		NamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		File baseDir = new File("target/test/ShapeWriterTest");
		
		nsManager.add("ex", "http://example.com/shape/");
		
		URI personShapeId = uri("http://example.com/shape/PersonShape");
		URI genderTypeShape = uri("http://example.com/shape/GenderTypeShape");
		
		Activity activity = new Activity();
		activity.setId(Activity.nextActivityId());
		activity.setType(Konig.GenerateEnumTables);
		activity.setEndTime(GregorianCalendar.getInstance());
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(personShapeId)
		.endShape()
		.beginShape(genderTypeShape)
			.wasGeneratedBy(activity)
		.endShape();
		
		FileGetter fileGetter = new ShapeFileGetter(baseDir, nsManager);
		
		Set<URI> activityWhitelist = new HashSet<>();
		activityWhitelist.add(Konig.GenerateEnumTables);
		Collection<Shape> shapeList = builder.getShapeManager().listShapes();
		
		ShapeWriter shapeWriter = new ShapeWriter();
		shapeWriter.writeGeneratedShapes(nsManager, shapeList, fileGetter, activityWhitelist);
		
		// TODO: implement assertions
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

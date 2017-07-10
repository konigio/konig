package io.konig.schemagen.bigquery;

/*
 * #%L
 * Konig Schema Generator
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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.pojo.PojoFactory;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.schemagen.gcp.BigQueryTable;
import io.konig.schemagen.gcp.BigQueryTableGenerator;
import io.konig.schemagen.gcp.GoogleCloudProject;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeNamer;
import io.konig.shacl.SimpleShapeNamer;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class BigQueryGeneratorTest {
	
	@Test
	public void testTableClass() throws Exception {
		Graph graph = new MemoryGraph();
		graph.edge(Schema.VideoObject, RDFS.SUBCLASSOF, Schema.MediaObject);
		graph.edge(Schema.MediaObject, RDFS.SUBCLASSOF, Schema.CreativeWork);
		graph.edge(Schema.WebPage, RDFS.SUBCLASSOF, Schema.CreativeWork);
		graph.edge(Schema.CreativeWork, RDFS.SUBCLASSOF, Schema.Thing);
		
		OwlReasoner owl = new OwlReasoner(graph);
		
		String aName = "http://example.com/v1/as/Activity";
		String bName = "http://example.com/v2/as/Activity";
		
		String videoShapeName = "http://example.com/v1/schema/VideoObject";
		String webPageShapeName = "http://example.com/v1/schema/WebPage";
		
		URI bitrate = uri("http://schema.org/bitrate");
		URI author = uri("http://schema.org/author");
		
		ShapeBuilder builder = new ShapeBuilder()
			.beginShape(aName)
				.targetClass(AS.Activity)
				.beginProperty(AS.object)
					.maxCount(1)
					.beginValueShape(videoShapeName)
						.targetClass(Schema.VideoObject)
						.beginProperty(bitrate)
							.datatype(XMLSchema.STRING)
							.minCount(1)
							.maxCount(1)
						.endProperty()
					.endValueShape()
				.endProperty()
			.endShape()
			.beginShape(bName)
				.targetClass(AS.Activity)
				.beginProperty(AS.object)
					.beginValueShape(webPageShapeName)
						.targetClass(Schema.WebPage)
						.beginProperty(author)
							.nodeKind(NodeKind.IRI)
							.valueClass(Schema.Person)
						.endProperty()
					.endValueShape()
				.endProperty()
			.endShape()
			;
		
		ShapeManager shapeManager = builder.getShapeManager();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		
		Shape aShape = builder.getShape(aName);
		
		ShapeNamer shapeNamer = new SimpleShapeNamer(nsManager, "http://example.com/dw/");
		BigQueryTableGenerator bigquery = new BigQueryTableGenerator(shapeManager, shapeNamer, owl);
		
		TableSchema table = bigquery.toTableSchema(aShape);
		table.setFactory(new JacksonFactory());
		
		System.out.println(table.toPrettyString());
		
		
	}
	

	private URI uri(String string) {
		return new URIImpl(string);
	}

	@Test
	public void testScan() throws Exception {
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		loadShape(shapeLoader, "src/test/resources/shapes/Membership-x1.ttl");
		loadShape(shapeLoader, "src/test/resources/shapes/Organization-x1.ttl");
		
		
		File sourceDir = new File("src/test/resources/bigquery");
		File outDir = new File("target/bigquery");
		outDir.mkdirs();
		BigQueryTableGenerator generator = new BigQueryTableGenerator(shapeManager);
		generator.writeTableDefinitions(sourceDir, outDir);
	}
	
	
	private void loadShape(ShapeLoader shapeLoader, String path) throws FileNotFoundException {
		InputStream stream = file(path);
		try {
			shapeLoader.loadTurtle(stream, null);
		} finally {
			close(stream);
		}
		
	}




	private void close(Closeable stream) {
		try {
			stream.close();
		} catch (IOException e) {
		}
		
	}


	private InputStream file(String path) throws FileNotFoundException {
		File file = new File(path);
		return new FileInputStream(file);
	}


	@Ignore
	public void test() throws Exception {
		
		MemoryGraph graph = new MemoryGraph();
		RdfUtil.loadTurtle(graph, resource("bigquery/gcp-project.ttl"), "");
		PojoFactory factory = new SimplePojoFactory();
		
		Vertex v = graph.v(Konig.GoogleCloudProject).in(RDF.TYPE).firstVertex();
		
		GoogleCloudProject project = factory.create(v, GoogleCloudProject.class);
		BigQueryTable table = project.findProjectDataset("test-dataset").findDatasetTable("Organization");
		
		JsonFactory jsonFactory = new JsonFactory();
		StringWriter buffer = new StringWriter();
		JsonGenerator json = jsonFactory.createGenerator(buffer);
		json.useDefaultPrettyPrinter();
		
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadTurtle(resource("shapes/Organization-x1.ttl"), null);

		BigQueryTableGenerator generator = new BigQueryTableGenerator(shapeManager);
		generator.writeTableDefinition(table, json);
		json.flush();
		
		String text = buffer.toString();
//		System.out.println(text);
		
		validateOrganizationTable(text, table);
		
	}

	private void validateOrganizationTable(String text, BigQueryTable table) throws JsonProcessingException, IOException {
		StringReader reader = new StringReader(text);
		BigQueryTableReference actualRef = table.getTableReference();
		
		ObjectMapper mapper = new ObjectMapper();
		
		ObjectNode json = (ObjectNode) mapper.readTree(reader);
		
		ObjectNode ref = (ObjectNode) json.get("tableReference");
		assertTrue(ref != null);
		assertEquals(actualRef.getProjectId(), ref.get("projectId").asText());
		assertEquals(actualRef.getDatasetId(), ref.get("datasetId").asText());
		assertEquals(actualRef.getTableId(), ref.get("tableId").asText());
		assertEquals(table.getDescription(), json.get("description").asText());
		
	}

	private InputStream resource(String path) {
		
		return getClass().getClassLoader().getResourceAsStream(path);
	}
	

}

package io.konig.gcp.deployment;

/*
 * #%L
 * Konig GCP Deployment Manager
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.GCP;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.schemagen.gcp.BigQueryTableGenerator;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class GcpConfigManagerTest {
	private Graph graph = new MemoryGraph();
	private ShapeManager shapeManager = new MemoryShapeManager();
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private BigQueryTableGenerator bigQueryTableGenerator = new BigQueryTableGenerator(shapeManager, null, reasoner);
	private File globalYaml = new File("src/test/resources/gcp/templates/global.yaml");
	
	private GcpConfigManager configManager = new GcpConfigManager(bigQueryTableGenerator, globalYaml);
	private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	
	@SuppressWarnings("unchecked")
	@Ignore
	public void testBasic() throws Exception {
		
		
		GoogleBigQueryTable table = new GoogleBigQueryTable();
		BigQueryTableReference tableReference = new BigQueryTableReference("${projectId}", "edw", "Person");
		table.setTableReference(tableReference);
		
		Shape personShape = new Shape(uri("http://example.com/shape/PersonShape"));
		shapeManager.addShape(personShape);
		personShape.addShapeDataSource(table);
		
		URI datasetIri = configManager.datasetIri("edw");
		
		graph.edge(datasetIri, GCP.location, literal("us-east1"));
		
		configManager.build(graph, shapeManager);
		
		StringWriter out = new StringWriter();
		configManager.write(out);
		
		String text = out.toString();

		ObjectMap actual = new ObjectMap( mapper.readValue(text.getBytes(), Map.class) );
		
		ObjectMap ds = actual.objectList("resources").stream()
				.filter(x -> "dataset-edw".equals(x.stringValue("name")))
				.findAny()
				.get();
		
		assertEquals("gcp-types/bigquery-v2:datasets", ds.stringValue("type"));
		
		ObjectMap properties = ds.objectValue("properties");
		assertEquals("edw", properties.objectValue("datasetReference").stringValue("datasetId"));
		assertEquals("us-east1", properties.stringValue("location"));
		

		ObjectMap landing = actual.objectList("resources").stream()
				.filter(x -> "${gcpProjectId}-test-landing".equals(x.stringValue("name")))
				.findAny()
				.get();
		
		assertTrue(landing != null);
		assertEquals("storage.v1.bucket", landing.stringValue("type"));
	}
	
	@Test
	public void testBigqueryStagingTable() throws Exception {
		load("src/test/resources/GcpConfigManagerTest/workbook-bigquery-staging-table");
		
		configManager.build(graph, shapeManager);
		
		StringWriter out = new StringWriter();
		configManager.write(out);
		
		String text = out.toString();
		
		@SuppressWarnings("unchecked")
		ObjectMap root = new ObjectMap( mapper.readValue(text.getBytes(), Map.class) );
		ObjectMap actual = root.objectList("resources").stream()
			.filter(x -> "bigquery-schema-Person".equals(x.stringValue("name")))
			.findAny()
			.get();
		
		
		assertEquals("CSV", actual
			.objectValue("properties")
			.objectValue("externalDataConfiguration")
			.stringValue("sourceFormat")
		);
		assertEquals(1, actual
			.objectValue("properties")
			.objectValue("externalDataConfiguration")
			.stringList("sourceUris").size()
		);
		assertEquals("gs://${gcpProjectId}-edw-landing/edw.staging.person.csv/*", actual
			.objectValue("properties")
			.objectValue("externalDataConfiguration")
			.stringList("sourceUris").get(0)
		);
	}
	
	private void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		GcpShapeConfig.init();
		File sourceDir = new File(path);
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

	private Literal literal(String value) {
		return new LiteralImpl(value);
	}
}

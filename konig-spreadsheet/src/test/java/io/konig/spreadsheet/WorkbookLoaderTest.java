package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.AWS;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.VANN;
import io.konig.core.vocab.VAR;
import io.konig.core.vocab.XOWL;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyPath;
import io.konig.shacl.SequencePath;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.io.ShapeWriter;

public class WorkbookLoaderTest {
	
	@Test
	public void testBigQueryTableIdRegex() throws Exception {

		GcpShapeConfig.init();
		InputStream input = getClass().getClassLoader().getResourceAsStream("bigQueryTableId-regex.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.setFailOnErrors(true);
		loader.load(book, graph);
		
		StringWriter writer = new StringWriter();
		RdfUtil.prettyPrintTurtle(graph, writer);
		
		writer.close();
		
		URI shapeId = uri("http://example.com/shapes/BqPersonShape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		
		assertTrue(shape.getShapeDataSource()!=null);
		
		List<DataSource> dsList = shape.getShapeDataSource().stream()
			.filter(ds -> ds.isA(Konig.GoogleBigQueryTable))
			.collect(Collectors.toList());
		
		assertEquals(1, dsList.size());
		
		DataSource ds = dsList.get(0);
		assertTrue(ds instanceof GoogleBigQueryTable);
		GoogleBigQueryTable table = (GoogleBigQueryTable) ds;
		String tableId = table.getTableReference().getTableId();
		assertTrue(tableId != null);
		assertEquals("BqPerson", tableId);
	}

	@Test
	public void testGoogleCloudSql() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("google-cloud-sql.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		
		StringWriter writer = new StringWriter();
		RdfUtil.prettyPrintTurtle(graph, writer);
		
		writer.close();
		
		// TODO: Add assertions
	}

	@Test
	public void testBigQueryTransform() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("bigquery-transform.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		Shape shape = shapeManager.getShapeById(uri("http://example.com/shapes/OriginProductShape"));
		URI predicate = uri("http://example.com/ns/alias/PRD_PRICE");
		PropertyConstraint p = shape.getPropertyConstraint(predicate);
		assertTrue(p!=null);
		Path path = p.getEquivalentPath();
		assertEquals("/schema:offers[schema:priceCurrency \"USD\"]/schema:price", path.toSimpleString());
	}
	@Test
	public void testCustomTableName() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("custom-tablename.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		
		StringWriter writer = new StringWriter();
		RdfUtil.prettyPrintTurtle(graph, writer);
		
		writer.close();
		URI shapeId = uri("https://schema.pearson.com/shapes/OriginSales_TeamShape");
		
		ShapeManager s = loader.getShapeManager();
		
		
		Shape shape = s.getShapeById(shapeId);
		assertTrue(shape!=null);
		List<DataSource> list = shape.getShapeDataSource();
		assertEquals(2, list.size());
		
		DataSource ds = list.get(0);
		// CASE 1 : Custom Table Name from the Datasource Function Parameter
		assertEquals("http://www.konig.io/ns/aws/host/${awsAuroraHost}/databases/schema1/tables/P6_Sales_Team", 
				ds.getId().stringValue());
		
		URI originCompanySubTypeShapeId = uri("https://schema.pearson.com/shapes/OriginCompanySubTypeShape");
		
		Shape originCompanySubTypeShape = s.getShapeById(originCompanySubTypeShapeId);
		assertTrue(originCompanySubTypeShape!=null);
		List<DataSource> originCompanySubTypeList = originCompanySubTypeShape.getShapeDataSource();
		assertEquals(2, originCompanySubTypeList.size());
		
		// CASE 2 : Custom Table Name from the global settings
		DataSource ds1 = originCompanySubTypeList.get(0);
		assertEquals("http://www.konig.io/ns/aws/host/${awsAuroraHost}/databases/schema1/tables/CompanySubType", 
				ds1.getId().stringValue());
		
		URI creativeWorkShapeShapeId = uri("https://schema.pearson.com/shapes/CreativeWorkShape");
		Shape creativeWorkShape = s.getShapeById(creativeWorkShapeShapeId);
		List<DataSource> dsList = creativeWorkShape.getShapeDataSource();
		
		// CASE 3 : Custom Table Name from the default settings.properties
		DataSource ds2 = dsList.get(0);
		assertEquals("https://www.googleapis.com/bigquery/v2/projects/${gcpProjectId}/datasets/schema/tables/CreativeWork", 
				ds2.getId().stringValue());
		
		
	}
	@Test
	public void testInvalidOntologyNamespace() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("invalid-ontology-namespace.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		Throwable error = null;
		try {
			loader.load(book, graph);
		} catch (Throwable e) {
			error = e;
		}
		assertTrue(error != null);
		assertTrue(error.getMessage().contains("Namespace must end with '/' or '#' but found: http://schema.org"));
	}
	
	@Test
	public void testGoogleCloudSqlTable() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("google-cloud-sql-table.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		List<DataSource> list = shape.getShapeDataSource();
		assertEquals(1, list.size());
		
		DataSource ds = list.get(0);
		assertEquals("https://www.googleapis.com/sql/v1beta4/projects/${gcpProjectId}/instances/schema/databases/schema/tables/PersonShape", 
				ds.getId().stringValue());
		
		assertTrue(ds.isA(Konig.GoogleCloudSqlTable));
	}
	
	@Test
	public void testDatasourceParamsGoogleBucket() throws Exception {
		GcpShapeConfig.init();
        InputStream input = new FileInputStream(new File("src/test/resources/test-datasource-params-bucket.xlsx"));
        Workbook book = WorkbookFactory.create(input);
        
        Graph graph = new MemoryGraph();
        NamespaceManager nsManager = new MemoryNamespaceManager();
        graph.setNamespaceManager(nsManager);
        
        WorkbookLoader loader = new WorkbookLoader(nsManager);
        loader.load(book, graph);
        input.close();
        
        
        URI shapeId = uri("http://example.com/shapes/ProductShape");
        
        ShapeManager shapeManager = loader.getShapeManager();
        Shape shape = shapeManager.getShapeById(shapeId);
        assertTrue(shape != null);
        
        List<String> eventTypes = shape.getShapeDataSource().stream()
        	.filter( e -> e instanceof GoogleCloudStorageBucket)
        	.map(e -> (GoogleCloudStorageBucket) e)
        	.findFirst().get().getNotificationInfo().get(0).getEventTypes();
        
        
        assertEquals(3, eventTypes.size());
        assertEquals("OBJECT_METADATA_UPDATE", eventTypes.get(0));
    }
	
	@Test
	public void testDatasourceParentComponent() throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream("test-datasource-params-parentComponent.xlsx");
        Workbook book = WorkbookFactory.create(input);
        Graph graph = new MemoryGraph();
        NamespaceManager nsManager = new MemoryNamespaceManager();
        graph.setNamespaceManager(nsManager);
        
        WorkbookLoader loader = new WorkbookLoader(nsManager);
        loader.load(book, graph);
        
        input.close();
        URI shapeId = uri("https://schema.pearson.com/shapes/AccountShape");
        ShapeManager s = loader.getShapeManager();
		
        Shape shape = s.getShapeById(shapeId);
		List<DataSource> list = shape.getShapeDataSource();
		assertEquals(2, list.size());
		DataSource ds = list.get(1);
		assertEquals("http://schema.pearson.com/ns/system/mdm",ds.getIsPartOf().get(0).toString());
		assertEquals("http://schema.pearson.com/ns/system/edw",ds.getIsPartOf().get(1).toString());
    }
	
	
	@Test
	public void testGoogleOracleTable() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("omcs-oracle-table.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		ShapeManager s = loader.getShapeManager();
		
		
		Shape shape = s.getShapeById(shapeId);
		assertTrue(shape!=null);
		List<DataSource> list = shape.getShapeDataSource();
		assertEquals(1, list.size());
		
		DataSource ds = list.get(0);
		assertEquals("http://www.konig.io/ns/omcs/instances/test/databases/schema/tables/Person", 
				ds.getId().stringValue());
		
		assertTrue(ds.isA(Konig.OracleTable));
	}
	
	@Test
	public void testAssessmentEndeavor() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("assessment-endeavor.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://schema.pearson.com/shapes/AssessmentEndeavorShape");
		
		
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		PropertyConstraint p = shape.getPropertyConstraint(AS.actor);
		assertTrue(p != null);
		assertTrue(p.getShape()==null);
		assertEquals(NodeKind.IRI, p.getNodeKind());
	}
	
	@Test
	public void testAddressCountry() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("address-country.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		List<PropertyConstraint> propertyList = shape.getProperty();
		SequencePath sequence = null;
		for (PropertyConstraint p : propertyList) {
			PropertyPath path = p.getPath();
			if (path instanceof SequencePath) {
				sequence = (SequencePath) path;
			}
		}
		
		assertTrue(sequence != null);
		
		assertEquals(2, sequence.size());
		PropertyPath address = sequence.get(0);
		assertTrue(address instanceof PredicatePath);
		PredicatePath predicatePath = (PredicatePath) address;
		assertEquals(Schema.address, predicatePath.getPredicate());
		
		PropertyPath country = sequence.get(1);
		assertTrue(country instanceof PredicatePath);
		predicatePath = (PredicatePath)country;
		assertEquals(Schema.addressCountry, predicatePath.getPredicate());
	}

	
	@Test
	public void testSequencePath() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("sequence-path.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		
		SequencePath sequence = null;
		for (PropertyConstraint p : shape.getVariable()) {
			PropertyPath path = p.getPath();
			if (path instanceof SequencePath) {
				if (sequence == null) {
					sequence = (SequencePath) path;
				} else {
					fail("Expected only one sequence path, but found more than one");
				}
			}
		}
		
		if (sequence == null) {
			fail("SequencePath not found");
		}
		
		assertEquals(2, sequence.size());
		
		PropertyPath first = sequence.get(0);
		assertTrue(first instanceof PredicatePath);
		PredicatePath predicatePath = (PredicatePath) first;
		assertEquals(uri(VAR.NAMESPACE + "?x"), predicatePath.getPredicate());
		
		PropertyPath second = sequence.get(1);
		assertTrue(second instanceof PredicatePath);
		predicatePath = (PredicatePath) second;
		assertEquals(Schema.givenName, predicatePath.getPredicate());
		
	}

	@Test
	public void testIriReference() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("iri-reference.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shape/PersonShape");
		
		Shape shape = loader.getShapeManager().getShapeById(shapeId);
		assertTrue(shape != null);
		assertEquals(shape.getNodeKind(), NodeKind.IRI);
		
		PropertyConstraint p = shape.getPropertyConstraint(Schema.memberOf);
		assertTrue(p != null);
		
		assertEquals(p.getNodeKind(), NodeKind.IRI);
	}
	
	
	@Test
	public void testLabels() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("labels.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		input.close();
		String arabicValue = "\u0627\u0644\u0627\u0633\u0645 \u0627\u0644\u0645\u0639\u0637\u0649";
		
		assertLabel(graph, Schema.givenName, "Given Name", "en");
		assertLabel(graph, Schema.givenName, "Pr\u00E9nom", "fr");
		assertLabel(graph, Schema.givenName, arabicValue, "ar");
		
	}
	
	private void assertLabel(Graph graph, URI subject, String label, String language) {
		Literal literal = new LiteralImpl(label, language);
		assertTrue(graph.contains(subject, RDFS.LABEL, literal));
	}

	@Test
	public void testPubSub() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("pubsub.xlsx");
		Workbook book = WorkbookFactory.create(input);

		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		input.close();
		
	
		
		URI topicId = uri("https://pubsub.googleapis.com/v1/projects/${gcpProjectId}/topics/vnd.example.person");
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		
		Shape shape = loader.getShapeManager().getShapeById(shapeId);
		assertTrue(shape != null);
		DataSource topic = 
				shape.getShapeDataSource().stream()
				.filter(s -> s.isA(Konig.GooglePubSubTopic))
				.findFirst().get();
		
		assertEquals(topicId, topic.getId());
		
	}
	
	@Test
	public void testSubproperty() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("subproperty.xlsx");
		Workbook book = WorkbookFactory.create(input);

		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		input.close();
		
		assertTrue(graph.contains(Schema.taxID, RDFS.SUBPROPERTYOF, Schema.identifier));
		assertTrue(graph.contains(Schema.identifier, RDF.TYPE, OWL.DATATYPEPROPERTY));
		
	}
	
	@Test
	public void testDefaultShape() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("default-shape.xlsx");
		Workbook book = WorkbookFactory.create(input);

		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		input.close();
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		
		
		List<URI> appList = shape.getDefaultShapeFor();
		assertTrue(appList != null);
		assertEquals(2, appList.size());
		assertTrue(appList.contains(uri("http://example.com/applications/MyCatalog")));
		assertTrue(appList.contains(uri("http://example.com/applications/MyShoppingCart")));
	}
	
	@Test
	public void testAggregateFunction() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("aggregate-function.xlsx");
		Workbook book = WorkbookFactory.create(input);

		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		input.close();
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		
		URI shapeId = uri("http://example.com/shapes/AssessmentMetricsShape");
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		
		PropertyConstraint p = shape.getVariableByName("?x");
		assertTrue(p != null);
		URI AssessmentResult = uri("http://example.com/ns/core/AssessmentResult");
		assertEquals(AssessmentResult, p.getValueClass());
		
	}
	
	
	@Test
	public void testDatasourceParams() throws Exception {
		GcpShapeConfig.init();
		InputStream input = getClass().getClassLoader().getResourceAsStream("test-datasource-params.xlsx");
		Workbook book = WorkbookFactory.create(input);

		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/ProductShape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		Shape shape = shapeManager.getShapeById(shapeId);
		
		List<DataSource> dsList = shape.getShapeDataSource().stream()
				.filter(ds -> ds.isA(Konig.GoogleBigQueryTable))
				.collect(Collectors.toList());
			
		assertEquals(1, dsList.size());
		
		DataSource ds = dsList.get(0);
		assertTrue(ds instanceof GoogleBigQueryTable);
		GoogleBigQueryTable table = (GoogleBigQueryTable) ds;
		String tableId = table.getTableReference().getTableId();
		assertTrue(tableId != null);
		assertEquals("CustomProduct", tableId);
		
		
	}
	
	@Test
	public void testIriTemplate() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("test-iri-template.xlsx");
		Workbook book = WorkbookFactory.create(input);

		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/ProductShape");
		Shape shape = loader.getShapeManager().getShapeById(shapeId);
		
		IriTemplate template = shape.getIriTemplate();
		
		String expected =
			"@context {\n" + 
			"   \"schema\" : \"http://schema.org/\",\n" + 
			"   \"name\" : \"schema:name\"\n" + 
			"}\n" + 
			"\n" + 
			"<http://example.com/product/{name}>";
		
		assertTrue(template != null);
		assertEquals(expected, template.toString());
		
	}
	
	@Test
	public void testDataSource() throws Exception {

		GcpShapeConfig.init();
		InputStream input = getClass().getClassLoader().getResourceAsStream("issue-161.xlsx");
		Workbook book = WorkbookFactory.create(input);

		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/PersonLiteShape");
		Shape shape = loader.getShapeManager().getShapeById(shapeId);
		
		assertTrue(shape != null);
		
		GoogleBigQueryTable table = shape.findDataSource(GoogleBigQueryTable.class);
		assertTrue(table != null);
		
		URI datasourceId = uri("https://www.googleapis.com/bigquery/v2/projects/${gcpProjectId}/datasets/schema/tables/Person");
		
		assertEquals(table.getId(), datasourceId);
		
		BigQueryTableReference tableRef = table.getTableReference();
		
		assertTrue(tableRef != null);
		
		assertEquals(tableRef.getProjectId(), "${gcpProjectId}");
		assertEquals(tableRef.getDatasetId(), "schema");
		assertEquals(tableRef.getTableId(), "Person");
	}
	

//	public void testOrConstraint() throws Exception {
//		InputStream input = getClass().getClassLoader().getResourceAsStream("or-constraint.xlsx");
//		Workbook book = WorkbookFactory.create(input);
//
//		Graph graph = new MemoryGraph();
//		NamespaceManager nsManager = new MemoryNamespaceManager();
//		
//		WorkbookLoader loader = new WorkbookLoader(nsManager);
//		
//		
//		loader.load(book, graph);
//		
//		ShapeManager shapeManager = new MemoryShapeManager();
//		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager);
//		shapeLoader.load(graph);
//		
//		URI shapeId = uri("http://example.com/shapes/v1/schema/PersonShape");
//		
//		Shape shape = shapeManager.getShapeById(shapeId);
//		assertTrue(shape != null);
//		
//		PropertyConstraint sponsor = shape.getPropertyConstraint(Schema.sponsor);
//		assertTrue(sponsor != null);
//		
//		Shape sponsorShape = sponsor.getShape();
//		assertTrue(sponsorShape != null);
//		
//		OrConstraint constraint = sponsorShape.getOr();
//		
//		assertTrue(constraint != null);
//		List<Shape> list = constraint.getShapes();
//		assertEquals(2, list.size());
//		
//	}
	
	@Test
	public void testPlaceData() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("place-data.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.setFailOnWarnings(false);
		loader.load(book, graph);

		URI placeId = uri("http://example.com/place/us");
		Vertex place = graph.getVertex(placeId);
		assertTrue(place != null);
		Vertex address = place.getVertex(Schema.address);
		assertTrue(address != null);
		assertValue(address, Schema.addressCountry, "US");
		
		placeId = uri("http://example.com/place/us/nj");
		place = graph.getVertex(placeId);
		assertTrue(place != null);
		address = place.getVertex(Schema.address);
		assertTrue(address != null);
		assertValue(address, Schema.addressCountry, "US");
		assertValue(address, Schema.addressRegion, "NJ");
	}
	
	@Test
	public void testEquivalentPath() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("analytics-model.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);

		URI shapeId = uri("http://example.com/shapes/SalesByCityShape");
		ShapeManager shapeManager = loader.getShapeManager();
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		
		
		URI state = uri("http://example.com/ns/alias/state");
		PropertyConstraint p = shape.getPropertyConstraint(state);
		String expected =
			"/<http://www.konig.io/ns/var/?x>/location[type State]";
		
		Path path = p.getEquivalentPath();
		
		assertEquals(expected, path.toSimpleString());

	}

	
	
	
	
	
	
//	public void testRollUpBy() throws Exception {
//
//		InputStream input = getClass().getClassLoader().getResourceAsStream("analytics-model.xlsx");
//		
//		Workbook book = WorkbookFactory.create(input);
//		Graph graph = new MemoryGraph();
//		NamespaceManager nsManager = new MemoryNamespaceManager();
//		
//		WorkbookLoader loader = new WorkbookLoader(nsManager);
//		
//		loader.load(book, graph);
//		
//		URI shapeId = uri("http://example.com/shapes/v1/fact/SalesByCountryShape");
//		Shape shape = loader.getShapeManager().getShapeById(shapeId);
//		assertTrue(shape != null);
//		
//		List<PropertyConstraint> list = shape.getProperty();
//		
//		assertEquals(4, list.size());
//		
//		PropertyConstraint totalCount = shape.getPropertyConstraint(Konig.totalCount);
//		assertTrue(totalCount != null);
//		assertEquals(Konig.measure, totalCount.getStereotype());
//		
//		PropertyConstraint country = shape.getPropertyConstraint(uri("http://example.com/ns/alias/country"));
//		assertTrue(country != null);
//		assertEquals(Konig.dimension, country.getStereotype());
//		assertTrue(country.getEquivalentPath() == null);
//		assertEquals("/alias:country", country.getFromAggregationSource());
//		
//		PropertyConstraint continent = shape.getPropertyConstraint(uri("http://example.com/ns/alias/continent"));
//		assertTrue(continent != null);
//		String expected = 
//			"@context {\n" + 
//			"  \"alias\" : \"http://example.com/ns/alias/\",\n" + 
//			"  \"country\" : \"alias:country\",\n" + 
//			"  \"schema\" : \"http://schema.org/\",\n" + 
//			"  \"containedInPlace\" : \"schema:containedInPlace\"\n" + 
//			"}\n" + 
//			"/country/containedInPlace";
//		assertEquals(expected, continent.getEquivalentPath().toString());
//		assertEquals("/alias:continent", continent.getFromAggregationSource());
//		
//		PropertyConstraint timeInterval = shape.getPropertyConstraint(Konig.timeInterval);
//		assertTrue(timeInterval != null);
//		assertEquals(uri("http://example.com/shapes/v1/konig/WeekMonthYearShape"), timeInterval.getShapeId());
//		
//	}
	
	
	
	
	
	@Test
	public void testStereotype() throws Exception {

		
		InputStream input = getClass().getClassLoader().getResourceAsStream("analytics-model.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);
		
		URI shapeId = uri("http://example.com/shapes/SalesByCityShape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape != null);
		
		
		PropertyConstraint totalCount = shape.getPropertyConstraint(Konig.totalCount);
		assertTrue(totalCount!=null);
		assertEquals(Konig.measure, totalCount.getStereotype());
		
		URI cityId = uri("http://example.com/ns/alias/city");
		PropertyConstraint city = shape.getPropertyConstraint(cityId);
		assertTrue(city != null);
		assertEquals(Konig.dimension, city.getStereotype());
		
//		RdfUtil.prettyPrintTurtle(nsManager, graph, new OutputStreamWriter(System.out));
		
	}

	@Test
	public void test() throws Exception {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream("person-model.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);

		
		checkOntologySheet(graph);
		checkNamespaces(nsManager);
		checkClasses(graph);
		checkProperties(graph);
		checkIndividuals(graph);
		checkShapes(loader);
		checkPropertyConstraints(loader);
		
		
	}
	
	@Test
	public void testAmazonRDSCluster() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("person-model-amazon-rds.xlsx");
		
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		
		loader.load(book, graph);		
		
		Vertex shape = graph.getVertex(uri("https://amazonaws.konig.io/rds/cluster/${environmentName}-test"));		
		
		assertValue(shape, AWS.dbClusterId, "${environmentName}-test");
		assertValue(shape, AWS.dbClusterName, "test");
		assertValue(shape, AWS.engine, "aurora");
		assertValue(shape, AWS.engineVersion, "5.6.10a");
		assertValue(shape, AWS.instanceClass, "db.r4.large");
		assertValue(shape, AWS.backupRetentionPeriod, "1");
		assertValue(shape, AWS.databaseName, "pearson-edw");
		assertValue(shape, AWS.dbSubnetGroupName, "default");
		assertValue(shape, AWS.preferredBackupWindow, "04:22-04:52");
		assertValue(shape, AWS.preferredMaintenanceWindow, "fri:06:44-fri:07:14");
		assertValue(shape, AWS.replicationSourceIdentifier, "arn:aws:rds:us-west-2:123456789012:cluster:aurora-cluster1");
		assertValue(shape, AWS.storageEncrypted, "0");
		List<Value> list = graph.v(uri("https://amazonaws.konig.io/rds/cluster/${environmentName}-test")).out(AWS.availabilityZone).toValueList();
		assertEquals(3, list.size());
	}

	@Test
	public void testAwsTable() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("awsAurora-transform.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/AuroraProductShape");
		
		ShapeManager s = loader.getShapeManager();
		
		
		Shape shape = s.getShapeById(shapeId);
		assertTrue(shape!=null);
		List<DataSource> list = shape.getShapeDataSource();
		assertEquals(1, list.size());
		DataSource ds = list.get(0);
		assertEquals("http://www.konig.io/ns/aws/host/devHost/databases/edwcore/tables/AuroraProductShape", 
				ds.getId().stringValue());
		assertTrue(ds.isA(Konig.AwsAuroraTable));
	}
	
	@Test
	public void testOneOfCol() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("awsAuroraTransformOneOF.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/PartyShape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		
		List<Shape> list = shape.getOr().getShapes();
		assertEquals(2, list.size());
		assertEquals("http://example.com/shapes/PersonShape", list.get(0).getId().stringValue());

	}
	
	@Test
	public void testPrimaryKey() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("primarykey-stereotype.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/SourcePersonShape");
		
		ShapeManager s = loader.getShapeManager();
		
				
		Shape shape = s.getShapeById(shapeId);
		List<PropertyConstraint> propertyList = shape.getProperty();
		//SequencePath sequence = null;
		for (PropertyConstraint p : propertyList) {
			//PropertyPath path = p.getPath();
			if (p.getStereotype() != null) {
				assertEquals((p.getStereotype().getLocalName()),"primaryKey");
			}
		}
		assertTrue(shape!=null);
	
		
	}
	
	@Test
	public void testSecurityClassification() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("security-classification.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		ShapeManager s = loader.getShapeManager();
			
		
		Shape shape = s.getShapeById(shapeId);
		assertTrue(shape!=null);
		

		PropertyConstraint familyName = shape.getPropertyConstraint(Schema.familyName);
		assertTrue(familyName != null);
		
		List<URI> qsc = familyName.getQualifiedSecurityClassification();
		assertTrue(qsc != null);
		assertEquals(1, qsc.size());
		
		URI Private = uri("http://example.com/ns/security/Private");
		assertEquals(Private, qsc.get(0));
		
		ShapeWriter shapeWriter = new ShapeWriter();
		try {
			shapeWriter.writeTurtle(nsManager, shape, new File("target/test/security-classification/PersonShape.ttl"));
		} catch (Exception e) {
			throw new KonigException(e);
		}
		
		// Verify that schema:givenName has the DCL4 security classification
		
		Set<URI> sc = graph.v(Schema.givenName).out(Konig.securityClassification).toUriSet();
		assertEquals(1, sc.size());
		
		URI DCL4 = uri("https://schema.pearson.com/ns/dcl/DCL4");
		assertEquals(DCL4, sc.iterator().next());

	}
	private void checkPropertyConstraints(WorkbookLoader loader) {
		
		ShapeManager shapeManager = loader.getShapeManager();
		URI shapeId = uri("http://example.com/shapes/v1/schema/Person");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape!=null);
		
		
		assertTrue(shape.getPropertyConstraint(Konig.id) == null);
		
		PropertyConstraint givenName = shape.getPropertyConstraint(Schema.givenName);
		
		assertEquals(givenName.getDatatype(), XMLSchema.STRING);
		assertEquals(givenName.getMinCount().intValue(), 0);
		assertEquals(givenName.getMaxCount().intValue(), 1);
		assertEquals(givenName.getTermStatus(), XOWL.Experimental);
		
		
		PropertyConstraint familyName = shape.getPropertyConstraint(Schema.familyName);
		assertEquals(familyName.getMinCount().intValue(), 1);
		
		PropertyConstraint address = shape.getPropertyConstraint(Schema.address);
		Shape addressValueShape = address.getShape();
		
		assertEquals(addressValueShape.getId(), uri("http://example.com/shapes/v1/schema/Address"));
		
		PropertyConstraint worksFor = shape.getPropertyConstraint(uri("http://schema.org/worksFor"));
		
		assertEquals(worksFor.getValueClass(), Schema.Organization);
		assertEquals(worksFor.getNodeKind(), NodeKind.IRI);
	}
	
	
	private Vertex propertyConstraint(Vertex shape, URI predicate) {
		return shape.asTraversal().out(SH.property).hasValue(SH.path, predicate).firstVertex();
	}
	
	private void assertInt(Vertex v, URI predicate, int value) {
		assertEquals(literal(v.getValue(predicate)).intValue(), value);
	}
	
	private Literal literal(Value value) {
		return (Literal) value;
	}


	private void checkShapes(WorkbookLoader loader) {
		ShapeManager shapeManager = loader.getShapeManager();
		URI shapeId = uri("http://example.com/shapes/v1/schema/Person");
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		assertTrue(shape.getType().contains(SH.Shape));
		
		assertEquals(shape.getComment(), "A light-weight data shape for a person.");
		assertEquals(shape.getTargetClass(), Schema.Person);
		assertEquals(shape.getNodeKind(), NodeKind.IRI);
		assertEquals(shape.getMediaTypeBaseName(), "application/vnd.example.v1.schema.person");
		
		
	}


	private void checkIndividuals(Graph graph) {

		URI OrderStatus = uri("http://schema.org/OrderStatus");
		Vertex v = graph.getVertex(uri("http://schema.org/OrderPaymentDue"));
		assertTrue(v!=null);
		
		assertValue(v, RDF.TYPE, OrderStatus);
		assertValue(v, RDFS.COMMENT, "Payment is due");
		assertValue(v, Schema.name, "Payment Due");
		assertValue(v, DCTERMS.IDENTIFIER, "40");
		
		
	}


	private void checkProperties(Graph graph) {
		

		Vertex v = graph.getVertex(Schema.givenName);
		assertTrue(v!=null);
		
		assertValue(v, RDFS.LABEL, "Given Name");
		assertValue(v, RDFS.COMMENT, "The person's given name. In the U.S., the first name of a Person. "
				+ "This can be used along with familyName instead of the name property.");
		assertValue(v, XOWL.termStatus, XOWL.Stable);
		assertValue(v, RDF.TYPE, RDF.PROPERTY);
		assertValue(v, RDF.TYPE, OWL.DATATYPEPROPERTY);
		assertValue(v, RDFS.DOMAIN, Schema.Person);
		assertValue(v, RDFS.RANGE, XMLSchema.STRING);
		
		v = graph.getVertex(Schema.address);
		assertValue(v, RDF.TYPE, OWL.OBJECTPROPERTY);
		
		
	}


	private void checkClasses(Graph graph) {
		
		Vertex v = graph.getVertex(Schema.Person);
		assertTrue(v!=null);
		
		assertValue(v, RDF.TYPE, OWL.CLASS);
		assertValue(v, RDFS.LABEL, "Person");
		assertValue(v, RDFS.COMMENT, "Any person (alive, dead, undead or fictional).");
		assertValue(v, RDFS.SUBCLASSOF, Schema.Thing);
		assertValue(v, XOWL.termStatus, XOWL.Stable);
		
	}


	private void checkNamespaces(NamespaceManager nsManager) {
		
		Namespace ns = nsManager.findByPrefix("owl");
		assertEquals("owl", ns.getPrefix());
		assertEquals(OWL.NAMESPACE, ns.getName());
		
		
	}


	private void checkOntologySheet(Graph graph) {
		
		Vertex v = graph.getVertex(uri(OWL.NAMESPACE));
		assertTrue(v != null);
		
		assertValue(v, RDF.TYPE, OWL.ONTOLOGY);
		assertValue(v, VANN.preferredNamespacePrefix, "owl");
		assertValue(v, RDFS.LABEL, "OWL");
		assertValue(v, RDFS.COMMENT, "Web Ontology Language");
		
	}
	


	private void assertValue(Vertex subject, URI predicate, String object) {
		Literal value = new LiteralImpl(object);
		assertValue(subject, predicate, value);
	}


	private void assertValue(Vertex subject, URI predicate, Value object) {
		
		List<Value> list = subject.asTraversal().out(predicate).toValueList();
		
		for (Value value : list) {
			if (value.equals(object)) {
				return;
			}
		}
		
		String message = MessageFormat.format("Triple not found: <{0}> <{1}> \"{2}\"", subject.getId().stringValue(), predicate.getLocalName(), object.stringValue());
		
		fail(message);
		
	}


	private URI uri(String text) {
		return new URIImpl(text);
	}

}

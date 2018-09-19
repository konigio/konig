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
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.SkosEmitter;
import io.konig.core.util.IOUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.AS;
import io.konig.core.vocab.AWS;
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
import io.konig.gcp.datasource.GoogleBigQueryView;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.schema.Person;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyPath;
import io.konig.shacl.SequencePath;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeWriter;

public class WorkbookLoaderTest {
	@Rule
    public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testDefaultDataSource() throws Exception {

		GcpShapeConfig.init();
		InputStream input = getClass().getClassLoader().getResourceAsStream("default-data-source.xlsx");
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
		
		URI shapeId = uri("https://schema.pearson.com/shapes/PERSON_STG_Shape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		Shape shape = shapeManager.getShapeById(shapeId);
		
		List<DataSource> list = shape.getShapeDataSource();
		assertTrue(list != null);
		assertTrue(list.size()==2);
		
		assertTrue(list.stream().filter(ds -> ds.isA(Konig.GoogleBigQueryTable)).count()==1);
		assertTrue(list.stream().filter(ds -> ds.isA(Konig.GoogleCloudStorageBucket)).count()==1);
	}

	@Test
	public void testDictionaryDefaultDatatype() throws Exception {

		GcpShapeConfig.init();
		InputStream input = getClass().getClassLoader().getResourceAsStream("dictionary-default-datatype.xlsx");
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
		
		URI shapeId = uri("https://schema.pearson.com/shapes/PERSON_STG_Shape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		URI predicate = uri("https://schema.pearson.com/ns/alias/FIRST_NAME");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		PropertyConstraint p = shape.getPropertyConstraint(predicate);
		assertEquals(XMLSchema.STRING, p.getDatatype());
	}
	
	@Test
	public void testDictionaryDefaultMaxLength() throws Exception {

		GcpShapeConfig.init();
		InputStream input = getClass().getClassLoader().getResourceAsStream("dictionary-max-length.xlsx");
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
		
		URI shapeId = uri("https://schema.pearson.com/shapes/PERSON_STG_Shape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		URI predicate = uri("https://schema.pearson.com/ns/alias/FIRST_NAME");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		PropertyConstraint p = shape.getPropertyConstraint(predicate);
		Integer maxLength = p.getMaxLength();
		assertTrue(maxLength != null);
		assertEquals(new Integer(256), maxLength);
		
		Integer minLength = p.getMinLength();
		assertTrue(minLength != null);
		assertEquals(new Integer(0), minLength);
	}
	
	
	@Test
	public void testSecurityClassificationByName() throws Exception {

		GcpShapeConfig.init();
		InputStream input = getClass().getClassLoader().getResourceAsStream("security-classification-by-name.xlsx");
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
		
		URI shapeId = uri("https://schema.pearson.com/shapes/PERSON_STG_Shape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		URI predicate = uri("https://schema.pearson.com/ns/alias/FIRST_NAME");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		PropertyConstraint p = shape.getPropertyConstraint(predicate);
		
		List<URI> list = p.getQualifiedSecurityClassification();
		URI dcl3 = uri("https://schema.pearson.com/ns/dcl/DCL3");
		URI pii = uri("https://schema.pearson.com/ns/dcl/PII");
		assertTrue(list.contains(dcl3));
		assertTrue(list.contains(pii));
		
	}
	
	
	@Test
	public void testCurrentStateView() throws Exception {

		GcpShapeConfig.init();
		InputStream input = getClass().getClassLoader().getResourceAsStream("current-state-view.xlsx");
		File file = new File("src/test/resources/current-state-view.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.setFailOnErrors(true);
		loader.load(book, graph,file);
		
		StringWriter writer = new StringWriter();
		RdfUtil.prettyPrintTurtle(graph, writer);
		
		writer.close();
		
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		

		
		GoogleBigQueryView datasource = shape.getShapeDataSource().stream()
				.filter(ds -> ds instanceof GoogleBigQueryView)
				.map(ds -> (GoogleBigQueryView) ds)
				.findFirst()
				.get();
		
		assertTrue(datasource != null);
		
	}
	
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
		File file = new File("src/test/resources/bigQueryTableId-regex.xlsx");
		loader.load(book, graph,file);
		
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
		File file = new File("src/test/resources/google-cloud-sql.xlsx");
		loader.load(book, graph,file);
		
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
		File file = new File("src/test/resources/bigquery-transform.xlsx");
		loader.load(book, graph,file);
		
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
		File file = new File("src/test/resources/custom-tablename.xlsx");
		loader.load(book, graph,file);
		
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
			File file = new File("src/test/resources/invalid-ontology-namespace.xlsx");
			loader.load(book, graph,file);
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
		File file = new File("src/test/resources/google-cloud-sql-table.xlsx");
		loader.load(book, graph,file);
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
        File file = new File("src/test/resources/test-datasource-params-bucket.xlsx");
        loader.load(book, graph,file);
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
        File file = new File("src/test/resources/test-datasource-params-parentComponent.xlsx");
        loader.load(book, graph,file);
        
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
		File file = new File("src/test/resources/omcs-oracle-table.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/assessment-endeavor.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/address-country.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/sequence-path.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/iri-reference.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/labels.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/pubsub.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/subproperty.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/default-shape.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/aggregate-function.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/test-datasource-params.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/test-iri-template.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/issue-161.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/place-data.xlsx");
		loader.load(book, graph,file);

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
		File file = new File("src/test/resources/analytics-model.xlsx");
		loader.load(book, graph,file);

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
		File file = new File("src/test/resources/analytics-model.xlsx");
		loader.load(book, graph,file);
		
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
		File file = new File("src/test/resources/person-model.xlsx");
		loader.load(book, graph,file);

		
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
		File file = new File("src/test/resources/person-model-amazon-rds.xlsx");
		loader.load(book, graph,file);		
		
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
		File file = new File("src/test/resources/awsAurora-transform.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/awsAuroraTransformOneOF.xlsx");
		loader.load(book, graph,file);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/PartyShape");
		
		ShapeManager shapeManager = loader.getShapeManager();
		
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		assertTrue(shape.getOr()!=null);
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
		File file = new File("src/test/resources/primarykey-stereotype.xlsx");
		loader.load(book, graph,file);
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
		File file = new File("src/test/resources/security-classification.xlsx");
		loader.load(book, graph,file);
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
	
	
	public void testTabularNodeShape_1() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("rdbms-node-shape.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		File file = new File("src/test/resources/rdbms-node-shape.xlsx");
		loader.load(book, graph,file);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/SourcePersonShape");
		
		ShapeManager s = loader.getShapeManager();
		
		ShapeWriter shapeWriter = new ShapeWriter();
		Shape shape = s.getShapeById(shapeId);
		
		assertTrue(shape!=null);
		try {
			shapeWriter.writeTurtle(nsManager, shape, new File("target/test/rdbmsNodeShape/PersonShape.ttl"));
		} catch (Exception e) {
			throw new KonigException(e);
		}
	}
	
	@Test
	public void testDataDictionaryForString() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("data-dictionary.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		File file = new File("src/test/resources/data-dictionary.xlsx");
		loader.load(book, graph,file);
		input.close();
		URI shapeId = uri("http://example.com/shapes/MDM/RA_CUSTOMER_TRX_ALL");
		ShapeManager s = loader.getShapeManager();
		
		Shape shape1 = s.getShapeById(shapeId);
		assertTrue(shape1!=null);
		//assertTrue(shape1.getType()!=null && shape1.getType().contains(Konig.TabularNodeShape));		
		
		//Test String property
		PropertyConstraint pc=shape1.getPropertyConstraint(uri("http://example.com/alias/ADDRESS_VERIFICATION_CODE"));
		assertTrue(pc!=null);
		assertTrue("Address Verification Code".equals(pc.getComment()));
		assertTrue(Konig.primaryKey.equals(pc.getStereotype()));
		assertTrue(XMLSchema.STRING.equals(pc.getDatatype()));
		assertTrue(pc.getMinCount()==1);
		assertTrue(pc.getMaxCount()==1);
		assertTrue(pc.getMaxLength()==80);
		assertTrue(pc.getMaxInclusive()==null);
		assertTrue(pc.getMinInclusive()==null);
		assertTrue(pc.getDecimalScale()==null);
		assertTrue(pc.getDecimalPrecision()==null);
		Person person = pc.getDataSteward();
		assertTrue(person != null);
		assertEquals("Sample Data Steward", person.getName());
		assertTrue(pc.getQualifiedSecurityClassification()!=null && pc.getQualifiedSecurityClassification()
				.contains(uri("https://schema.pearson.com/ns/dcl/DCL1")));
		
		pc=shape1.getPropertyConstraint(uri("http://example.com/alias/NAME"));
		assertTrue(XMLSchema.STRING.equals(pc.getDatatype()) && pc.getMaxLength()==80);
		assertTrue(pc.getQualifiedSecurityClassification()!=null && pc.getQualifiedSecurityClassification().contains(uri("https://schema.pearson.com/ns/dcl/DCL3")));
		pc=shape1.getPropertyConstraint(uri("http://example.com/alias/GENDER"));
		assertTrue(XMLSchema.STRING.equals(pc.getDatatype()) && pc.getMaxLength()==1);
		pc=shape1.getPropertyConstraint(uri("http://example.com/alias/ADDRESS"));
		assertTrue(XMLSchema.STRING.equals(pc.getDatatype()) && pc.getMaxLength()==80);
		assertTrue(pc!=null);
		
		shapeId = uri("http://example.com/shapes/ORACLE_EBS/OE_ORDER_LINES_ALL");
		Shape shape2 = s.getShapeById(shapeId);
		assertTrue(shape2!=null);
	}
	
	@Test
	public void testDataDictionaryForInteger() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("data-dictionary.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		File file = new File("src/test/resources/data-dictionary.xlsx");
		loader.load(book, graph,file);
		input.close();
		URI shapeId = uri("http://example.com/shapes/MDM/RA_CUSTOMER_TRX_ALL");
		ShapeManager s = loader.getShapeManager();
			
		
		Shape shape1 = s.getShapeById(shapeId);
		assertTrue(shape1!=null);
		//assertTrue(shape1.getType()!=null && shape1.getType().contains(Konig.TabularNodeShape));
		
		shapeId = uri("http://example.com/shapes/ORACLE_EBS/OE_ORDER_LINES_ALL");
		Shape shape2 = s.getShapeById(shapeId);
		assertTrue(shape2!=null);
		
		PropertyConstraint pc=shape2.getPropertyConstraint(uri("http://example.com/alias/ORDER_DATE"));
		assertTrue(XMLSchema.DATE.equals(pc.getDatatype()) && pc.getMaxLength()==null);
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/ORDER_TIME"));
		assertTrue(XMLSchema.DATETIME.equals(pc.getDatatype()) && pc.getMaxLength()==null);
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/INVOICE_VALUE"));
		
		assertTrue(XMLSchema.DECIMAL.equals(pc.getDatatype()));
		assertEquals(15, pc.getDecimalPrecision().intValue());
		assertEquals(6, pc.getDecimalScale().intValue());
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/DICOUNT_PC"));
		
		assertTrue(XMLSchema.INTEGER.equals(pc.getDatatype()) && pc.getMinInclusive().intValue()==-32768 
				&& pc.getMaxInclusive().intValue()==32767);
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/NO_OF_ITEMS"));
		
		assertTrue(XMLSchema.INTEGER.equals(pc.getDatatype()) && pc.getMinInclusive().intValue()==0 
				&& pc.getMaxInclusive().intValue()==65535);
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/CREDIT_VALUE"));
		
		assertTrue(XMLSchema.INTEGER.equals(pc.getDatatype()) && 
				pc.getMinInclusive().intValue()==-2147483648 && pc.getMaxInclusive().intValue()==2147483647);
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/ORDER_NUMBER"));
		
		assertTrue(XMLSchema.INTEGER.equals(pc.getDatatype()) && pc.getMinInclusive().intValue()==0 
				&& pc.getMaxInclusive().longValue() == 4294967295L);
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/TOTAL_VALUE"));
		
		assertTrue(XMLSchema.INTEGER.equals(pc.getDatatype()) && (pc.getMinInclusive().longValue() == -9223372036854775808L) 
				&& (pc.getMaxInclusive().longValue() == 9223372036854775807L));
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/INVOICE_NUMBER"));
		assertTrue(XMLSchema.INTEGER.equals(pc.getDatatype()) && pc.getMinInclusive().intValue()==0
				&& pc.getMaxInclusive().equals(new BigInteger("18446744073709551615")));
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/DISCOUNT_AMOUNT"));
		assertTrue(XMLSchema.FLOAT.equals(pc.getDatatype()));
		assertTrue(pc.getMinInclusive()==null);
		assertTrue(pc.getMaxInclusive()==null);
		assertTrue(pc.getDecimalPrecision()!=null);
		assertEquals(15, pc.getDecimalPrecision().intValue());
		assertTrue(pc.getDecimalScale() != null);
		assertEquals(6, pc.getDecimalScale().intValue());
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/VALUE_OF_ITEMS"));
		assertTrue(XMLSchema.DOUBLE.equals(pc.getDatatype()));
		assertTrue(pc.getMinInclusive()==null);
		assertTrue(pc.getMaxInclusive()==null);
		assertTrue(pc.getDecimalPrecision()!=null);
		assertEquals(15, pc.getDecimalPrecision().intValue());
		assertTrue(pc.getDecimalScale() != null);
		assertEquals(6, pc.getDecimalScale().intValue());
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/SERVICE_TAX_APPLIED"));
		assertTrue(XMLSchema.DECIMAL.equals(pc.getDatatype()));
		assertTrue(pc.getMinInclusive()==null);
		assertTrue(pc.getMaxInclusive()==null);
		assertTrue(pc.getDecimalPrecision()!=null);
		assertEquals(15, pc.getDecimalPrecision().intValue());
		assertTrue(pc.getDecimalScale() != null);
		assertEquals(6, pc.getDecimalScale().intValue());
		
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/CODE"));
		assertTrue(XMLSchema.BASE64BINARY.equals(pc.getDatatype()) && pc.getMaxLength()==5);
		pc=shape2.getPropertyConstraint(uri("http://example.com/alias/IS_NEW"));
		assertTrue(XMLSchema.BOOLEAN.equals(pc.getDatatype()));
	}
	
	@Test
	public void testDataDictionaryForAbbreviations() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("data-dictionary.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		File file = new File("src/test/resources/data-dictionary.xlsx");
		loader.load(book, graph,file);
		input.close();
		URI shapeId = uri("http://example.com/shapes/MDM/RA_CUSTOMER_TRX_ALL");
		ShapeManager s = loader.getShapeManager();
		
		// We'll also test the SkosEmitter in this test case.

		File testDir = new File("target/test/WorkbookLoaderTest/dataDictionaryAbbrev");
		SkosEmitter emitter = new SkosEmitter(testDir);
		emitter.emit(graph);
		
		graph = new MemoryGraph(nsManager);
		RdfUtil.loadTurtle(testDir, graph);
		
		Shape shape1 = s.getShapeById(shapeId);
		assertTrue(shape1!=null);
		assertTrue(shape1.getUsesAbbreviationScheme()!=null && 
				uri("https://example.com/exampleModel/AbbreviationScheme/").equals(shape1.getUsesAbbreviationScheme()));		
		
		shapeId = uri("http://example.com/shapes/ORACLE_EBS/OE_ORDER_LINES_ALL");
		Shape shape2 = s.getShapeById(shapeId);
		assertTrue(shape2!=null);
		assertTrue(shape2.getUsesAbbreviationScheme()!=null && 
				uri("https://example.com/exampleModel/AbbreviationScheme/").equals(shape2.getUsesAbbreviationScheme()));	
		
		List<Vertex> vertexList = graph.v(SKOS.CONCEPT_SCHEME).in(RDF.TYPE).toVertexList();
		assertTrue(vertexList!=null && vertexList.size()==1);
		Vertex abbrev=vertexList.get(0);
		assertTrue(uri("https://example.com/exampleModel/AbbreviationScheme/").equals(abbrev.getId()));
		
		vertexList = graph.v(SKOS.CONCEPT).in(RDF.TYPE).toVertexList();
		assertTrue(vertexList!=null && vertexList.size()==3);
		Vertex mfgVertex=vertexList.get(0);
		assertTrue(uri("https://example.com/exampleModel/AbbreviationScheme/MANUFACTURING").equals(mfgVertex.getId()));
		ValueFactory vf=new ValueFactoryImpl();
		assertTrue(vf.createLiteral("MANUFACTURING").equals(mfgVertex.getValue(SKOS.PREF_LABEL)));
		assertTrue(vf.createLiteral("MFG").equals(mfgVertex.getValue(Konig.abbreviationLabel)));
		assertTrue(vf.createURI("https://example.com/exampleModel/AbbreviationScheme/").equals(mfgVertex.getValue(SKOS.IN_SCHEME)));
		
		Vertex uda=vertexList.get(1);
		assertTrue(uri("https://example.com/exampleModel/AbbreviationScheme/USER_DEFINED_ATTRIBUTES").equals(uda.getId()));
		assertTrue(vf.createLiteral("USER_DEFINED_ATTRIBUTES").equals(uda.getValue(SKOS.PREF_LABEL)));
		assertTrue(vf.createLiteral("UDA").equals(uda.getValue(Konig.abbreviationLabel)));
		assertTrue(vf.createURI("https://example.com/exampleModel/AbbreviationScheme/").equals(uda.getValue(SKOS.IN_SCHEME)));
		
		Vertex org=vertexList.get(2);
		assertTrue(uri("https://example.com/exampleModel/AbbreviationScheme/ORGANIZATION").equals(org.getId()));
		assertTrue(vf.createLiteral("ORGANIZATION").equals(org.getValue(SKOS.PREF_LABEL)));
		assertTrue(vf.createLiteral("ORG").equals(org.getValue(Konig.abbreviationLabel)));
		assertTrue(vf.createURI("https://example.com/exampleModel/AbbreviationScheme/").equals(org.getValue(SKOS.IN_SCHEME)));
		
		IOUtil.recursiveDelete(testDir);
		
	}
	@Test
	public void testDataDictionaryForDatasource() throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream("data-dictionary.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		URI shapeId = uri("http://example.com/shapes/MDM/RA_CUSTOMER_TRX_ALL");
		ShapeManager s = loader.getShapeManager();
			
		
		Shape shape1 = s.getShapeById(shapeId);
		assertTrue(shape1!=null);
		assertTrue(shape1.getShapeDataSource()!=null);
		List<DataSource> datasourceList=shape1.getShapeDataSource();
		assertTrue(datasourceList.size()==1);
		assertTrue(datasourceList.get(0).getType()!=null && datasourceList.get(0).getType().size()==2);
		assertTrue(datasourceList.get(0).getType().contains(Konig.AwsAuroraTable));
		
		shapeId = uri("http://example.com/shapes/ORACLE_EBS/OE_ORDER_LINES_ALL");
		Shape shape2 = s.getShapeById(shapeId);
		assertTrue(shape2!=null);	
		assertTrue(shape2.getShapeDataSource()!=null);
		datasourceList=shape2.getShapeDataSource();
		assertTrue(datasourceList.size()==1);
		assertTrue(datasourceList.get(0).getType()!=null && datasourceList.get(0).getType().size()==2);
		assertTrue(datasourceList.get(0).getType().contains(Konig.AwsAuroraTable));
		
	}
	
	public void testTabularNodeShape_2() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("rdbms-node-shape-generator.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		File file = new File("src/test/resources/rdbms-node-shape-generator.xlsx");
		loader.load(book, graph,file);
		input.close();
		URI shapeId = uri("http://example.com/shapes/TargetPersonRdbmsShape");
		
		ShapeManager s = loader.getShapeManager();
		
		
		ShapeWriter shapeWriter = new ShapeWriter();
		Shape shape = s.getShapeById(shapeId);
		System.out.println(shape.getTabularOriginShape()+" shape.getTabularOriginShape()");
		assertTrue(shape!=null);
		try {	
			shapeWriter.writeTurtle(nsManager, shape, new File("target/test/rdbmsNodeShape/Shape_TargetShape.ttl"));
		} catch (Exception e) {
			throw new KonigException(e);
		}
	}
	@Test
	public void testDecimalDataType() throws Exception {

		InputStream input = getClass().getClassLoader().getResourceAsStream("decimal-datatype.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.setFailOnErrors(true);
		File file = new File("src/test/resources/decimal-datatype.xlsx");
		thrown.expect(RuntimeException.class);
		thrown.expectCause(CoreMatchers.isA(SpreadsheetException.class));		
		thrown.expectMessage("Property alias:ISDELETED is missing required decimal scale on row 17 in workbook decimal-datatype.xlsx");
		loader.load(book, graph, file);
	}
	public void testRelationshipDegree() throws Exception{


		InputStream input = getClass().getClassLoader().getResourceAsStream("relationship-degree.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		Vertex genderTypeClass=graph.getVertex(uri("http://schema.org/GenderType"));
		assertTrue(genderTypeClass!=null);
		Vertex subClassOf = genderTypeClass.getVertex(RDFS.SUBCLASSOF);
		assertTrue(OWL.RESTRICTION.equals(subClassOf.getURI(RDF.TYPE)));
		assertTrue(uri("http://example.com/ns/demo/genderCode").equals(subClassOf.getURI(OWL.ONPROPERTY)));
		assertTrue(Konig.OneToOne.equals(subClassOf.getURI(Konig.relationshipDegree)));
		
		Vertex imageObjectClass=graph.getVertex(uri("http://schema.org/ImageObject"));
		assertTrue(imageObjectClass!=null);		
		List<Vertex> subClassOfList = imageObjectClass.asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
		assertTrue(OWL.RESTRICTION.equals(subClassOfList.get(1).getURI(RDF.TYPE)));
		assertTrue(uri("http://schema.org/thumbnail").equals(subClassOfList.get(1).getURI(OWL.ONPROPERTY)));
		assertTrue(Konig.OneToMany.equals(subClassOfList.get(1).getURI(Konig.relationshipDegree)));
		
		Vertex videoObjectClass=graph.getVertex(uri("http://schema.org/VideoObject"));
		assertTrue(videoObjectClass!=null);
		subClassOfList = videoObjectClass.asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
		assertTrue(OWL.RESTRICTION.equals(subClassOfList.get(1).getURI(RDF.TYPE)));
		assertTrue(uri("http://schema.org/thumbnail").equals(subClassOfList.get(1).getURI(OWL.ONPROPERTY)));
		assertTrue(Konig.OneToMany.equals(subClassOfList.get(1).getURI(Konig.relationshipDegree)));	
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
		assertEquals(XOWL.Experimental, givenName.getTermStatus());
		
		
		PropertyConstraint familyName = shape.getPropertyConstraint(Schema.familyName);
		assertEquals(familyName.getMinCount().intValue(), 1);
		
		PropertyConstraint address = shape.getPropertyConstraint(Schema.address);
		Shape addressValueShape = address.getShape();
		
		assertEquals(addressValueShape.getId(), uri("http://example.com/shapes/v1/schema/Address"));
		
		PropertyConstraint worksFor = shape.getPropertyConstraint(uri("http://schema.org/worksFor"));
		
		assertEquals(worksFor.getValueClass(), Schema.Organization);
		assertEquals(worksFor.getNodeKind(), NodeKind.IRI);
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
		assertValue(v, Konig.termStatus, XOWL.Stable);
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
		assertValue(v, Konig.termStatus, XOWL.Stable);
		
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

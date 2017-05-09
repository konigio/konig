package io.konig.transform.sql.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.path.PathFactory;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.DmlExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.transform.TransformFrame;
import io.konig.transform.TransformFrameBuilder;

public class QueryBuilderTest {
	
	private ShapeManager shapeManager = new MemoryShapeManager();
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private PathFactory pathFactory = new PathFactory(nsManager, graph);
	private TransformFrameBuilder frameBuilder = new TransformFrameBuilder(shapeManager, pathFactory);
	private QueryBuilder queryBuilder = new QueryBuilder(graph);
	
	@Before
	public void setUp() {
		GcpShapeConfig.init();
	}
	
	@Test
	public void testHasValueConstraint() throws Exception {
		load("src/test/resources/QueryBuilderTest/hasValueConstraint");
		URI targetShapeId = uri("http://example.com/shapes/BqProductShape");
		

		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		TransformFrame frame = frameBuilder.create(targetShape);

		SelectExpression select = queryBuilder.selectExpression(frame);
		
		String actual = toText(select);
		
		String expected = 
			"SELECT\n" + 
			"   STRUCT(\n" + 
			"      PRD_PRICE AS price,\n" + 
			"      \"USD\" AS priceCurrency\n" + 
			"   ) AS offers\n" + 
			"FROM schema.OriginProductShape";
		
		assertEquals(expected, actual);
	}

	@Test
	public void testInsertNestFields() throws Exception {

		loadShapes("QueryBuilderTest/testInsertNestFields.ttl");
		
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		TransformFrame frame = frameBuilder.create(targetShape);

		
		BigQueryCommandLine cmd = queryBuilder.insertCommand(frame);
		
		assertTrue(cmd != null);
		DmlExpression dml = cmd.getDml();
		
		String actual = dml.toString();
		
		String expected = 
				"INSERT warehouse.Person (id, givenName, familyName, address)\n" + 
				"SELECT\n" + 
				"   a.id,\n" + 
				"   a.first_name AS givenName,\n" + 
				"   a.last_name AS familyName,\n" + 
				"   STRUCT(\n" + 
				"      a.city AS addressLocality,\n" + 
				"      a.state AS addressRegion\n" + 
				"   ) AS address\n" + 
				"FROM staging.Person AS a\n" + 
				"WHERE NOT EXISTS(\n" + 
				"   SELECT id\n" + 
				"   FROM warehouse.Person AS b\n" + 
				"   WHERE b.id=a.id)\n" + 
				"";
		
		assertEquals(expected, actual);
		
	}

	@Test
	public void testInsertWithTransformedKey() throws Exception {
		loadShapes("QueryBuilderTest/testInsertWithTransformedKey.ttl");
		
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
	
		BigQueryCommandLine cmd = queryBuilder.insertCommand(frame);
		
		assertTrue(cmd != null);
		DmlExpression dml = cmd.getDml();
		
		String actual = dml.toString();
		
		String expected = 
			"INSERT ex.Person (id, ldapKey, email, modified)\n" + 
			"SELECT\n" + 
			"   CONCAT(\"http://example.com/person/\", a.personKey) AS id,\n" + 
			"   a.personKey AS ldapKey,\n" + 
			"   a.email,\n" + 
			"   TIMESTAMP(\"{modified}\") AS modified\n" + 
			"FROM ex.SourcePersonShape AS a\n" + 
			"WHERE NOT EXISTS(\n" + 
			"   SELECT personKey\n" + 
			"   FROM ex.Person AS b\n" + 
			"   WHERE b.ldapKey=a.personKey)\n" + 
			"";
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testInsert() throws Exception {
		loadShapes("QueryBuilderTest/testInsert.ttl");
		
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
	
		BigQueryCommandLine cmd = queryBuilder.insertCommand(frame);
		
		assertTrue(cmd != null);
		DmlExpression dml = cmd.getDml();
		
		String actual = dml.toString();
		
		String expected = 
			"INSERT ex.Person (id, ldapKey, email, modified)\n" + 
			"SELECT\n" + 
			"   CONCAT(\"http://example.com/person/\", a.ldapKey) AS id,\n" + 
			"   a.ldapKey,\n" + 
			"   a.email,\n" + 
			"   TIMESTAMP(\"{modified}\") AS modified\n" + 
			"FROM ex.SourcePersonShape AS a\n" + 
			"WHERE NOT EXISTS(\n" + 
			"   SELECT ldapKey\n" + 
			"   FROM ex.Person AS b\n" + 
			"   WHERE b.ldapKey=a.ldapKey)\n" + 
			"";
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testModifiedTimestamp() throws Exception {
		loadShapes("QueryBuilderTest/testModifiedTimestamp.ttl");
		
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
	
		
		SelectExpression dml = queryBuilder.selectExpression(frame);
		assertTrue(dml != null);
		
		String actual = dml.toString();
		
		String expected = 
			"SELECT\n" + 
			"   CONCAT(\"http://example.com/person/\", ldapKey) AS id,\n" + 
			"   TIMESTAMP(\"{modified}\") AS modified\n" + 
			"FROM ex.SourcePersonShape";
		
		assertEquals(expected, actual);
	}

	@Test
	public void testJoinKeyToEquivalentKey() throws Exception {
		loadShapes("QueryBuilderTest/testJoinKeyToEquivalentKey.ttl");
		
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
	
		
		SelectExpression dml = queryBuilder.selectExpression(frame);
		assertTrue(dml != null);
		
		String actual = dml.toString();
		
		String expected = 
			"SELECT\n" + 
			"   CONCAT(\"http://example.com/person/\", a.ldapKey) AS id,\n" + 
			"   STRUCT(\n" + 
			"      b.name\n" + 
			"   ) AS worksFor\n" + 
			"FROM \n" + 
			"   ex.SourcePersonShape AS a\n" + 
			" JOIN\n" + 
			"   ex.Organization AS b\n" + 
			" ON\n" + 
			"   a.employerKey=b.key";
		
		assertEquals(expected, actual);
	}

	@Test
	public void testJoinKeyToKey() throws Exception {
		loadShapes("QueryBuilderTest/testJoinKeyToKey.ttl");
		
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
	
		
		SelectExpression dml = queryBuilder.selectExpression(frame);
		assertTrue(dml != null);
		
		String actual = dml.toString();
		
		String expected = 
			"SELECT\n" + 
			"   CONCAT(\"http://example.com/person/\", a.ldapKey) AS id,\n" + 
			"   STRUCT(\n" + 
			"      b.name\n" + 
			"   ) AS worksFor\n" + 
			"FROM \n" + 
			"   ex.SourcePersonShape AS a\n" + 
			" JOIN\n" + 
			"   ex.Organization AS b\n" + 
			" ON\n" + 
			"   a.employerKey=b.ldapKey";
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testTypeOfNestedRecord() throws Exception {
		loadShapes("QueryBuilderTest/testTypeOfNestedRecord.ttl");
		
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
		
		// Let's validate that we are getting the expected SqlFrame...
//		SqlFrameFactory sqlFactory = new SqlFrameFactory();
//		SqlFrame sqlFrame = sqlFactory.create(frame);
//		
//		SqlAttribute worksFor = sqlFrame.getAttributes().get(0);
//		assertEquals(Schema.worksFor, worksFor.getAttribute().getPredicate());
//		
//		URI employerType = uri("http://example.com/alias/employerType");
//		MappedProperty m = worksFor.getMappedProperty();
//		assertEquals(employerType, m.getProperty().getPredicate());
//		assertEquals(0, m.getStepIndex());
//		
//		SqlFrame worksForFrame = worksFor.getEmbedded();
//		assertTrue(worksForFrame != null);
//		
//		SqlAttribute type = worksForFrame.getAttributes().get(0);
//		assertEquals(RDF.TYPE, type.getAttribute().getPredicate());
//		
//		m = type.getMappedProperty();
//		assertEquals(employerType, m.getProperty().getPredicate());
//		assertEquals(1, m.getStepIndex());
//		
//		TransformAttribute typeTarget = type.getAttribute();
//		PropertyConstraint p = typeTarget.getTargetProperty();
//		Resource valueClass = p.getValueClass();
//		URI organizationType = uri("http://example.com/ns/OrganizationType");
//		assertEquals(organizationType, valueClass);
		
	
		
		SelectExpression dml = queryBuilder.selectExpression(frame);
		assertTrue(dml != null);
		
		String actual = dml.toString();
		
		String expected = 
			"SELECT\n" + 
			"   STRUCT(\n" + 
			"      CASE employerType\n" + 
			"         WHEN \"edu\" THEN \"EducationalOrganization\"\n" + 
			"         WHEN \"com\" THEN \"Corporation\"\n" + 
			"      END AS type\n" + 
			"   ) AS worksFor\n" + 
			"FROM ex.SourcePersonShape";
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testDerivedProperty() throws Exception {

		loadShapes("QueryBuilderTest/testDerivedProperty.ttl");
		
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
		
		SelectExpression dml = queryBuilder.selectExpression(frame);
		assertTrue(dml != null);
		
		String actual = dml.toString();
		
		String expected = 
			"SELECT\n" + 
			"   IF(email=\"alice@example.com\" , 1 , 0) + IF(award=\"Best in Show\" , 1 , 0) AS answerCount\n" + 
			"FROM acme.OriginPerson";
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testEnum() throws Exception {
		loadShapes("QueryBuilderTest/testEnum.ttl");
		
		URI targetShapeId = uri("http://example.com/shapes/PersonTargetShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
		
		SelectExpression dml = queryBuilder.selectExpression(frame);
		assertTrue(dml != null);
		
		String actual = dml.toString();
		
		String expected = 
			"SELECT\n" + 
			"   id,\n" + 
			"   CASE gender\n" + 
			"      WHEN \"M\" THEN \"Male\"\n" + 
			"      WHEN \"F\" THEN \"Female\"\n" + 
			"   END AS gender\n" + 
			"FROM acme.OriginPerson";
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testUpdate() throws Exception {
		loadShapes("QueryBuilderTest/testUpdate.ttl");
		
		URI targetShapeId = uri("http://example.com/shapes/PersonLiteShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
		
		BigQueryCommandLine command = queryBuilder.updateCommand(frame);
		DmlExpression dml = command.getDml();
		assertTrue(dml != null);
		
		String actual = dml.toString();
		
		String expected = 
			"UPDATE acme.Person AS a\n" + 
			"SET\n" + 
			"   a.givenName = b.givenName,\n" + 
			"   a.familyName = b.familyName,\n" + 
			"   a.address = STRUCT(\n" + 
			"      b.postalCode\n" + 
			"   ) AS address\n" + 
			"FROM acme.PersonStaging AS b\n" + 
			"WHERE a.id=CONCAT(\"http://example.com/person/\", b.identifier)";
		
		assertEquals(expected, actual);
	}

	@Test
	public void testJoin() throws Exception  {
		loadShapes("QueryBuilderTest/testJoin.ttl");
		
		URI targetShapeId = uri("http://example.com/shape/EmployeeShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
		
		
		SelectExpression select = queryBuilder.selectExpression(frame);
		
		String actual = toText(select);
		
		String expected = 
			"SELECT\n" + 
			"   a.id,\n" + 
			"   a.givenName,\n" + 
			"   a.familyName,\n" + 
			"   STRUCT(\n" + 
			"      b.name\n" + 
			"   ) AS worksFor\n" + 
			"FROM \n" + 
			"   directory.Person AS a\n" + 
			" JOIN\n" + 
			"   directory.Organization AS b\n" + 
			" ON\n" + 
			"   a.worksFor=b.id";
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testCourseInstance() throws Exception  {
		loadShapes("QueryBuilderTest/testCourseInstance.ttl");
		
		URI targetShapeId = uri("http://example.com/shape/CourseInstanceReportingShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
		
		SelectExpression select = queryBuilder.selectExpression(frame);
		
		String actual = toText(select);
		
		String expected = 
				"SELECT\n" + 
				"   CONCAT(\"http://example.com/section/\", section_id) AS id,\n" + 
				"   end_date AS endDate,\n" + 
				"   section_id AS registrarId,\n" + 
				"   start_date AS startDate,\n" + 
				"   section_name AS name\n" + 
				"FROM registrar.CourseInstance";
		
		assertEquals(expected, actual);
	}
	
	
	
	@Test
	public void testRole() throws Exception {

		loadShapes("QueryBuilderTest/testRole.ttl");
		
		URI targetShapeId = uri("http://example.com/shape/RoleReportingShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
		
		SelectExpression select = queryBuilder.selectExpression(frame);
		
		String actual = toText(select);
		
		String expected = "SELECT\n" + 
				"   CONCAT(\"http://example.com/role/\", role_name) AS id,\n" + 
				"   role_id AS registrarId,\n" + 
				"   role_name AS name\n" + 
				"FROM registrar.Role";
		
		assertEquals(expected, actual);
	}

	
	@Test
	public void testFilter() throws Exception {
		loadGraph("QueryBuilderTest/testFilter.ttl");
		
		Shape targetShape = shapeManager.getShapeById(uri("http://example.com/shapes/PersonLiteShape"));
		
		TransformFrame frame = frameBuilder.create(targetShape);
		SelectExpression select = queryBuilder.selectExpression(frame);
		
		String expected = 
			"SELECT\n" + 
			"   givenName,\n" + 
			"   familyName,\n" + 
			"   STRUCT(\n" + 
			"      postalCode\n" + 
			"   ) AS address\n" + 
			"FROM acme.PersonFull";
		
		String actual = toText(select);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testBigQueryCommandLine() throws Exception {
		loadGraph("QueryBuilderTest/testBigQueryCommandLine.ttl");
		
		Shape targetShape = shapeManager.getShapeById(uri("http://example.com/shapes/warehouse/PersonShape"));
		
		TransformFrame frame = frameBuilder.create(targetShape);
		
		BigQueryCommandLine cmd = queryBuilder.bigQueryCommandLine(frame);
		assertTrue(cmd != null);
		assertEquals("acme.Person", cmd.getDestinationTable());
	}
	
	@Test
	public void testEntityId() throws Exception {
		loadGraph("QueryBuilderTest/testEntityId.ttl");
		
		Shape targetShape = shapeManager.getShapeById(uri("http://example.com/shapes/PersonSemanticShape"));
		
		TransformFrame frame = frameBuilder.create(targetShape);
		SelectExpression select = queryBuilder.selectExpression(frame);
		
		String expected = 
			"SELECT\n" + 
			"   CONCAT(\"http://example.com/resources/person/\", person_id) AS id,\n" + 
			"   name\n" + 
			"FROM acme.OriginPerson";
		
		String actual = toText(select);
		
		assertEquals(expected, actual);
		
	}

	
	@Test
	public void testIriReference() throws Exception {
		loadGraph("QueryBuilderTest/testIriReference.ttl");
		
		Shape targetShape = shapeManager.getShapeById(uri("http://example.com/shapes/PersonWarehouseShape"));
		
		TransformFrame frame = frameBuilder.create(targetShape);
		SelectExpression select = queryBuilder.selectExpression(frame);
		
		String expected = 
			"SELECT\n" + 
			"   name,\n" + 
			"   CONCAT(\"http://example.com/resources/org/\", graduated_from) AS alumniOf\n" + 
			"FROM acme.OriginPerson";
		String actual = toText(select);
		
		
		assertEquals(expected, actual);
		
	}

	@Test
	public void testNestFields() throws Exception {
		loadGraph("QueryBuilderTest/testNestFields.ttl");
		
		Shape targetShape = shapeManager.getShapeById(uri("http://example.com/shapes/v2/schema/PersonShape"));
		
		TransformFrame frame = frameBuilder.create(targetShape);
		SelectExpression select = queryBuilder.selectExpression(frame);
		
		String expected = 
				"SELECT\n" + 
				"   first_name AS givenName,\n" + 
				"   last_name AS familyName,\n" + 
				"   STRUCT(\n" + 
				"      city AS addressLocality,\n" + 
				"      state AS addressRegion\n" + 
				"   ) AS address\n" + 
				"FROM staging.Person";
		String actual = toText(select);
		assertEquals(expected, actual);
		
	}
	
	@Test
	public void testRenameFields() throws Exception {
		loadGraph("QueryBuilderTest/testRenameFields.ttl");
		
		Shape targetShape = shapeManager.getShapeById(uri("http://example.com/shapes/warehouse/PersonShape"));
		
		TransformFrame frame = frameBuilder.create(targetShape);
		SelectExpression select = queryBuilder.selectExpression(frame);
		
		String expected = 
				"SELECT\n" + 
				"   first_name AS givenName,\n" + 
				"   last_name AS familyName\n" + 
				"FROM staging.Person";
		String actual = toText(select);
		
		assertEquals(expected, actual);
		
	}

	private String toText(SelectExpression select) {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter writer = new PrettyPrintWriter(buffer);
		select.print(writer);
		writer.close();
		
		return buffer.toString();
	}

	private void loadGraph(String filePath) throws Exception {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream(filePath);
		if (input == null) {
			throw new Exception("Resource not found: " + filePath);
		}
		RdfUtil.loadTurtle(graph, input, "");
		ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
		shapeLoader.load(graph);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}


	private void load(String path) throws Exception {
		File file = new File(path);
		RdfUtil.loadTurtle(file, graph);
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
	}
	
	private void loadShapes(String resource) throws Exception {
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		graph.setNamespaceManager(nsManager);
		RdfUtil.loadTurtle(graph, input, "");
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
	}
}

package io.konig.transform.sql.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.StringWriter;

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
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.SelectExpression;
import io.konig.transform.TransformFrame;
import io.konig.transform.TransformFrameBuilder;

public class QueryBuilderTest {
	
	private ShapeManager shapeManager = new MemoryShapeManager();
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private PathFactory pathFactory = new PathFactory(nsManager, graph);
	private TransformFrameBuilder frameBuilder = new TransformFrameBuilder(shapeManager, pathFactory);
	private QueryBuilder queryBuilder = new QueryBuilder();
	

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
			"   p.givenName,\n" + 
			"   p.familyName,\n" + 
			"   STRUCT(\n" + 
			"      o.name\n" + 
			"   ) AS worksFor\n" + 
			"FROM \n" + 
			"   directory.Person AS p\n" + 
			" JOIN\n" + 
			"   directory.Organization AS o\n" + 
			" ON\n" + 
			"   p.worksFor=o.id";
		
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
	
	@Ignore
	public void testMembership() throws Exception {

		loadShapes("QueryBuilderTest/testMembership.ttl");
		
		URI targetShapeId = uri("http://example.com/shape/MembershipReportingShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		
		TransformFrame frame = frameBuilder.create(targetShape);
		
		SelectExpression select = queryBuilder.selectExpression(frame);
		
		String actual = toText(select);
		
		String expected = "";
		
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

	private void loadShapes(String resource) throws Exception {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream(resource);
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.loadTurtle(input);
	}
}

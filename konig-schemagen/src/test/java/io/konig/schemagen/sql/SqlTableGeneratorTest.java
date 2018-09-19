package io.konig.schemagen.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.abbrev.AbbreviationConfig;
import io.konig.abbrev.AbbreviationManager;
import io.konig.abbrev.MemoryAbbreviationManager;
import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.project.Project;
import io.konig.core.project.ProjectFolder;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.omcs.datasource.OracleShapeConfig;
import io.konig.schemagen.SchemaGeneratorTest;
import io.konig.schemagen.aws.AwsAuroraTableWriter;
import io.konig.schemagen.gcp.CloudSqlTableWriter;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.io.ShapeWriter;
import io.konig.spreadsheet.WorkbookLoader;

public class SqlTableGeneratorTest extends SchemaGeneratorTest {

	
	private SqlTableGenerator generator = new SqlTableGenerator();

	private AbbreviationManager abbrevManager;


	@Test
	public void testKitchenSink() throws Exception {
		load("src/test/resources/sql-generator/sql-kitchen-sink");
		
		URI shapeId = iri("http://example.com/shapes/ThingShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		
		SqlTable table = generator.generateTable(shape,null);
		
		assertTrue(table!=null);
		
		assertField(table, "booleanProperty", FacetedSqlDatatype.BOOLEAN);
		assertField(table, "bitProperty", FacetedSqlDatatype.BIT);
		assertField(table, "unsignedTinyInt", FacetedSqlDatatype.UNSIGNED_TINYINT);
		assertField(table, "signedTinyInt", FacetedSqlDatatype.SIGNED_TINYINT);
		assertField(table, "unsignedSmallInt", FacetedSqlDatatype.UNSIGNED_SMALLINT);
		assertField(table, "signedSmallInt", FacetedSqlDatatype.SIGNED_SMALLINT);
		assertField(table, "unsignedMediumInt", FacetedSqlDatatype.UNSIGNED_MEDIUMINT);
		assertField(table, "signedMediumInt", FacetedSqlDatatype.SIGNED_MEDIUMINT);
		assertField(table, "unsignedInt", FacetedSqlDatatype.UNSIGNED_INT);
		assertField(table, "signedInt", FacetedSqlDatatype.SIGNED_INT);
		assertField(table, "date", FacetedSqlDatatype.DATE);
		assertField(table, "dateTime", FacetedSqlDatatype.DATETIME);
		assertStringField(table, "text", SqlDatatype.TEXT, 0);
		assertStringField(table, "char", SqlDatatype.CHAR, 32);
		assertStringField(table, "varchar", SqlDatatype.VARCHAR, 200);
		assertField(table, "float", FacetedSqlDatatype.SIGNED_FLOAT);
		assertField(table, "double", FacetedSqlDatatype.SIGNED_DOUBLE);
		
		//System.out.println(table.toString());
	}

	private void assertStringField(SqlTable table, String columnName, SqlDatatype datatype, int length ) {

		SqlColumn c = table.getColumnByName(columnName);
		assertTrue(c != null);
		assertTrue(c.getDatatype() instanceof StringSqlDatatype);
		StringSqlDatatype type = (StringSqlDatatype) c.getDatatype();
		assertEquals(length, type.getMaxLength());
		assertEquals(datatype, c.getDatatype().getDatatype());
	}

	private void assertField(SqlTable table, String columnName, FacetedSqlDatatype datatype) {
		SqlColumn c = table.getColumnByName(columnName);
		assertTrue(c != null);
		assertEquals(datatype, c.getDatatype());
		
	}
	
	private ProjectFolder folder(File baseDir) {
		Project project = new Project(uri("urn:maven:test-SqlTableGenerator-1.0"), baseDir.getParentFile());
		return new ProjectFolder(project, baseDir);
	}
	
	@Test
	public void testAWS() throws Exception {
		
		
		load("src/test/resources/aws");
		load("src/test/resources/aws_abbrev");
		

		File baseDir = new File("target/test/resources/aws/sql");
		baseDir.mkdirs();
		
		
		ProjectFolder folder = folder(baseDir);
		AwsAuroraTableWriter awsTableWriter = new AwsAuroraTableWriter(baseDir, new SqlTableGenerator(), folder,null);
		awsTableWriter.visit(shapeManager.listShapes().get(0));
		SqlTable table = generator.generateTable(shapeManager.listShapes().get(0),null);
		
		assertTrue(table!=null);
	}
	@Test
	public void testAWSWithAbbreviations() throws Exception {
		
		 		
		load("src/test/resources/aws_abbrev");
		

		File baseDir = new File("target/test/resources/aws_abbrev/sql");
		baseDir.mkdirs();

		ProjectFolder folder = folder(baseDir);
		AwsAuroraTableWriter awsTableWriter = new AwsAuroraTableWriter(baseDir, new SqlTableGenerator(), folder, abbrevManager());
		
		URI shapeId = uri("http://example.com/shapes/ORACLE_EBS/OE_ORDER_LINES_ALL");
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		
		awsTableWriter.visit(shape);
		SqlTable table = generator.generateTable(shapeManager.listShapes().get(0),abbrevManager());
		
		assertTrue(table!=null && "OE_ORDER_LINES_ALL".equals(table.getTableName()));
		assertTrue(table.getColumnList()!=null && table.getColumnList().size()==2);
		assertTrue(table.getColumnByName("ORGANIZATION_ID")==null && table.getColumnByName("ORG_ID")!=null);
		assertTrue(table.getColumnByName("LINE_ID")!=null);
		
	}
	private AbbreviationManager abbrevManager() {
		if (abbrevManager == null) {

			AbbreviationConfig config = new AbbreviationConfig();
			config.setDelimiters("_ \t\r\n");
			config.setPreferredDelimiter("_");
			abbrevManager = new MemoryAbbreviationManager(graph, config);
		}
		return abbrevManager;
	}

	@Test
	public void testGCP() throws Exception {
		
		GcpShapeConfig.init();
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadTurtle(resource("gcp/shape_PartyShape.ttl"), null);
		File baseDir = new File("target/test/resources/gcp/sql");
		baseDir.mkdirs();
		ProjectFolder folder = folder(baseDir);
		CloudSqlTableWriter cloudSqlTableWriter = new CloudSqlTableWriter( new SqlTableGenerator(),folder,null);
		cloudSqlTableWriter.visit(shapeManager.listShapes().get(0));
		SqlTable table = generator.generateTable(shapeManager.listShapes().get(0),null);
		
		assertTrue(table!=null);
		}
	
	@Test
	public void testGCPWithAbbreviations() throws Exception {
		

		load("src/test/resources/gcp_abbrev");
		
		URI shapeId = uri("http://example.com/shapes/MDM/RA_CUSTOMER_TRX_ALL");
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		
		File baseDir = new File("target/test/resources/gcp_abbrev/sql");
		baseDir.mkdirs();
		ProjectFolder folder = folder(baseDir);
		CloudSqlTableWriter cloudSqlTableWriter = new CloudSqlTableWriter( new SqlTableGenerator(),folder,abbrevManager());
		cloudSqlTableWriter.visit(shapeManager.listShapes().get(0));
		SqlTable table = generator.generateTable(shapeManager.listShapes().get(0),abbrevManager());
		
		assertTrue(table!=null && "RA_CUSTOMER_TRX_ALL".equals(table.getTableName()));
		assertTrue(table.getColumnList()!=null && table.getColumnList().size()==1);
		assertTrue(table.getColumnByName("MANUFACTURING_VERIFICATION_CODE")==null);
		assertTrue(table.getColumnByName("MFG_VERIFICATION_CODE")!=null);
	}
	@Test
	public void testSyntheticKeyForAwsAurora() throws Exception{
		AwsShapeConfig.init();
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadTurtle(resource("aws/shape_PersonRdbmsShape.ttl"), null);
		File baseDir = new File("target/test/resources/aws/sql");
		baseDir.mkdirs();
		ProjectFolder folder = folder(baseDir);
		CloudSqlTableWriter cloudSqlTableWriter = new CloudSqlTableWriter( new SqlTableGenerator(),folder,null);
		cloudSqlTableWriter.visit(shapeManager.listShapes().get(0));
		SqlTable table = generator.generateTable(shapeManager.listShapes().get(0),null);
		
		assertTrue(table!=null);
		assertTrue(table.getColumnList()!=null && !table.getColumnList().isEmpty());
		String query=table.toString();
		SqlColumn column=table.getColumnByName("PERSON_ID");
		assertTrue(column!=null && SqlKeyType.SYNTHETIC_KEY.equals(column.getKeytype()));				
		assertTrue(query.contains(column.getColumnName()+" INT NOT NULL AUTO_INCREMENT"));
		assertTrue(query.substring(query.indexOf("PRIMARY KEY")).contains(column.getColumnName()));
		
		
		
	}
	@Test
	public void testSyntheticKeyForCloudSql() throws Exception{
		GcpShapeConfig.init();
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadTurtle(resource("gcp/shape_PersonRdbmsShape.ttl"), null);
		File baseDir = new File("target/test/resources/gcp/sql");
		baseDir.mkdirs();
		ProjectFolder folder = folder(baseDir);
		CloudSqlTableWriter cloudSqlTableWriter = new CloudSqlTableWriter( new SqlTableGenerator(),folder,null);
		cloudSqlTableWriter.visit(shapeManager.listShapes().get(0));
		SqlTable table = generator.generateTable(shapeManager.listShapes().get(0),null);
		
		assertTrue(table!=null);
		assertTrue(table.getColumnList()!=null && !table.getColumnList().isEmpty());
		String query=table.toString();
		SqlColumn column=table.getColumnByName("PERSON_ID");
		assertTrue(column!=null && SqlKeyType.SYNTHETIC_KEY.equals(column.getKeytype()));			
		assertTrue(query.contains(column.getColumnName()+" INT NOT NULL AUTO_INCREMENT"));
		assertTrue(query.substring(query.indexOf("PRIMARY KEY")).contains(column.getColumnName()));
			
	}
	@Test
	public void testSyntheticKeyForOracle() throws Exception{
		OracleShapeConfig.init();
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadTurtle(resource("oracle/shape_PersonShape.ttl"), null);
		File baseDir = new File("target/test/resources/oracle/sql");
		baseDir.mkdirs();
		ProjectFolder folder = folder(baseDir);
		CloudSqlTableWriter cloudSqlTableWriter = new CloudSqlTableWriter( new SqlTableGenerator(),folder,null);
		cloudSqlTableWriter.visit(shapeManager.listShapes().get(0));
		Shape shape=shapeManager.listShapes().get(0);
		PropertyConstraint p=shape.getPropertyConstraint(new URIImpl("http://example.com/ns/alias/personId"));
		assertTrue(Konig.syntheticKey.equals(p.getStereotype()));
		SqlTable table = generator.generateTable(shape,null);
		
		assertTrue(table!=null);
		String query=table.toString();		
		SqlColumn column=table.getColumnByName("personId");
		assertTrue(column!=null && !SqlKeyType.SYNTHETIC_KEY.equals(column.getKeytype()));		
		assertFalse(query.contains(column.getColumnName()+" INT NOT NULL AUTO_INCREMENT"));
	}
	@Test
	public void testTextField() throws Exception{
		 AwsShapeConfig.init();
		 InputStream input = getClass().getClassLoader().getResourceAsStream("sql/SQL-DDL-Text.xlsx");
		 Workbook book = WorkbookFactory.create(input);
			Graph graph = new MemoryGraph();
			NamespaceManager nsManager = new MemoryNamespaceManager();
			graph.setNamespaceManager(nsManager);
			
			WorkbookLoader loader = new WorkbookLoader(nsManager);
			File file = new File("src/test/resources/sql/test-datasource-params-bucket.xlsx");
			loader.load(book, graph,file);
			input.close();
			URI shapeId = uri("http://example.com/shapes/MDM_PRODUCT");
			
			ShapeManager s = loader.getShapeManager();
			
			Shape shape = s.getShapeById(shapeId);
			assertTrue(shape!=null);
			ShapeWriter shapeWriter = new ShapeWriter();
			try {
				shapeWriter.writeTurtle(nsManager, shape, new File("target/test/sql/MDM_PRODUCT.ttl"));
			} catch (Exception e) {
				throw new KonigException(e);
			}
			
			load("target/test/sql/MDM_PRODUCT");
			
			assertTrue(shape!=null);
			
			SqlTable table = generator.generateTable(shape,null);
			
			assertTrue(table!=null);
			assertStringField(table, "PPID", SqlDatatype.VARCHAR, 2000);
			assertStringField(table, "PP_NAME", SqlDatatype.VARCHAR, 255);
			assertStringField(table, "ASSEMBLY_INSTRUCTIONS",SqlDatatype.VARCHAR, 1000);
			assertStringField(table, "LONG_DESCRIPTION", SqlDatatype.TEXT, 0);
	}
private InputStream resource(String path) {
		
		return getClass().getClassLoader().getResourceAsStream(path);
	}
private URI uri(String text) {
	return new URIImpl(text);
}
}

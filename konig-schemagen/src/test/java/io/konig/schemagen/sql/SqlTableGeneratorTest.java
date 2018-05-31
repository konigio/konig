package io.konig.schemagen.sql;

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
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.datasource.DatasourceFileLocator;
import io.konig.datasource.DdlFileLocator;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.schemagen.SchemaGeneratorTest;
import io.konig.schemagen.aws.AwsAuroraTableWriter;
import io.konig.schemagen.gcp.CloudSqlTableWriter;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.io.ShapeWriter;
import io.konig.spreadsheet.WorkbookLoader;

public class SqlTableGeneratorTest extends SchemaGeneratorTest {

	
	private SqlTableGenerator generator = new SqlTableGenerator();



	@Test
	public void testKitchenSink() throws Exception {
		load("src/test/resources/sql-generator/sql-kitchen-sink");
		
		URI shapeId = iri("http://example.com/shapes/ThingShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape!=null);
		
		SqlTable table = generator.generateTable(shape);
		
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
	@Test
	public void testAWS() throws Exception {
		
		 AwsShapeConfig.init();
		MemoryShapeManager shapeManager = new MemoryShapeManager();
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager, nsManager);
		shapeLoader.loadTurtle(resource("aws/shape_PartyShape.ttl"), null);
		

		File baseDir = new File("target/test/resources/aws/sql");
		baseDir.mkdirs();
		DatasourceFileLocator sqlFileLocator = new DdlFileLocator(new File(baseDir.toString()));
		AwsAuroraTableWriter awsTableWriter = new AwsAuroraTableWriter(baseDir, new SqlTableGenerator(), sqlFileLocator);
		awsTableWriter.visit(shapeManager.listShapes().get(0));
		SqlTable table = generator.generateTable(shapeManager.listShapes().get(0));
		
		assertTrue(table!=null);
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
		DatasourceFileLocator sqlFileLocator = new DdlFileLocator(new File(baseDir.toString()));
		CloudSqlTableWriter cloudSqlTableWriter = new CloudSqlTableWriter( new SqlTableGenerator(),sqlFileLocator);
		cloudSqlTableWriter.visit(shapeManager.listShapes().get(0));
		SqlTable table = generator.generateTable(shapeManager.listShapes().get(0));
		
		assertTrue(table!=null);
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
			loader.load(book, graph);
			input.close();
			URI shapeId = uri("http://example.com/shapes/MDM_PRODUCT");
			
			ShapeManager s = new MemoryShapeManager();
			
			ShapeLoader shapeLoader = new ShapeLoader(s);
			shapeLoader.load(graph);		
			
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
			
			SqlTable table = generator.generateTable(shape);
			
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

package io.konig.transform.showl.sql;

/*
 * #%L
 * Konig Transform
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ExplicitDerivedFromSelector;
import io.konig.core.showl.MappingStrategy;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlSourceNodeSelector;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.ValueExpression;

/**
 * These tests are not deterministic and may fail.  Need to rework the code to ensure deterministic behavior.
 * @author Greg McFall
 *
 */
public class ShowlSqlTransformTest {

	private ShowlManager showlManager;
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private MappingStrategy strategy = new MappingStrategy();
	private ShowlSqlTransform transform = new ShowlSqlTransform();
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private ShapeManager shapeManager = new MemoryShapeManager();
	
	private InsertStatement insert(String resourcePath, String shapeId) throws Exception {

		GcpShapeConfig.init();
		load(resourcePath);
		URI shapeIri = uri(shapeId);
		ShowlNodeShape node = showlManager.getNodeShape(shapeIri).findAny();

		strategy.selectMappings(node);
		
		return transform.createInsert(node, GoogleBigQueryTable.class);
	}

	@Test
	public void testMonthFunction() throws Exception {
		
		test(
			"src/test/resources/ShowlSqlTransformTest/month-function", 
			"http://example.com/ns/shape/PersonTargetShape");
	}
	
	@Test
	public void testCaseStatement() throws Exception {
		
		test(
			"src/test/resources/ShowlSqlTransformTest/case-statement", 
			"http://example.com/ns/shape/PersonTargetShape");
	}
	
	@Test
	public void testSubstrStrpos() throws Exception {
		
		test(
			"src/test/resources/ShowlSqlTransformTest/substr-strpos", 
			"http://example.com/ns/shape/PersonTargetShape");
	}
	
	@Test
	public void testSubstrFunction() throws Exception {
		
		test(
			"src/test/resources/ShowlSqlTransformTest/substr-function", 
			"http://example.com/ns/shape/PersonTargetShape");
	}
	
	@Test
	public void testJoinNameAndAddress() throws Exception {
		
		test(
			"src/test/resources/ShowlSqlTransformTest/join-name-and-address", 
			"http://example.com/ns/shape/PersonTargetShape");
	}
	
	@Test
	public void testInverseProperty() throws Exception {
		
		test(
			"src/test/resources/ShowlSqlTransformTest/inverse-property-transform", 
			"http://example.com/ns/shape/BookTargetShape");
	}

	@Test
	public void testNestedSourcePerson() throws Exception {
		
		test(
			"src/test/resources/ShowlSqlTransformTest/nested-source-transform", 
			"http://example.com/ns/shape/PersonTargetShape",
			"person.sql");
	}

	@Test
	public void testNestedSourceBook() throws Exception {
		
		test(
			"src/test/resources/ShowlSqlTransformTest/nested-source-transform", 
			"http://example.com/ns/shape/BookTargetShape",
			"book.sql");
	}


	@Test
	public void testCastConcat() throws Exception {
		
		test(
			"src/test/resources/ShowlSqlTransformTest/cast-concat-transform", 
			"http://example.com/ns/shape/PersonTargetShape");
	}
	
	private void test(String dataDir, String targetShapeId) throws Exception {
		test(dataDir, targetShapeId, "expected.sql");
	}

	private void test(String dataDir, String targetShapeId, String expectedDml) throws Exception {

		ShowlSourceNodeSelector selector = new ExplicitDerivedFromSelector();
		showlManager = new ShowlManager(shapeManager, reasoner, selector, null);
		
		InsertStatement insert = insert(dataDir, targetShapeId);
//		System.out.println(insert);
		
		assertFileEquals(
			dataDir + "/" + expectedDml,
			insert.toString());
	}

	@Test
	public void testConcat() throws Exception {
		
		ShowlSourceNodeSelector selector = new ExplicitDerivedFromSelector();
		showlManager = new ShowlManager(shapeManager, reasoner, selector, null);
		
		InsertStatement insert = insert(
				"src/test/resources/ShowlSqlTransformTest/concat-transform", 
				"http://example.com/ns/shape/PersonTargetShape");
		
		assertFileEquals(
			"src/test/resources/ShowlSqlTransformTest/concat-transform/expected.sql",
			insert.toString());
	}
	

	
	@Test
	public void testFlatToNested() throws Exception {
		
		ShowlSourceNodeSelector selector = new ExplicitDerivedFromSelector();
		showlManager = new ShowlManager(shapeManager, reasoner, selector, null);
		
		InsertStatement insert = insert(
				"src/test/resources/ShowlSqlTransformTest/flat-to-nested-transform", 
				"http://example.com/ns/shape/PersonTargetShape");

	}
	
	private void assertFileEquals(String expectedFile, String actualText) throws Exception {
		String[] actual = actualText.split("\\r?\\n");
		int index = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(expectedFile)))) {
			String expected = reader.readLine();
			assertEquals(expected, actual[index++]);
		}
		
	}


	@Test
	public void testTabular() throws Exception {
		
		InsertStatement insert = insert(
			"src/test/resources/ShowlSqlTransformTest/tabular-mapping", 
			"http://example.com/ns/shape/PersonTargetShape");
		
		assertEquals("schema.PersonTarget", insert.getTargetTable().getTableName());
		
		List<ColumnExpression> columns = insert.getColumns();
		assertEquals(2, columns.size());
		SelectExpression select = insert.getSelectQuery();
		
		List<ValueExpression> values = select.getValues();
		assertEquals("CONCAT(\"http://example.com/person/\", a.person_id) AS id", values.get(0).toString());
		assertEquals("a.first_name AS givenName", values.get(1).toString());
		
		List<TableItemExpression> from = select.getFrom().getTableItems();
		assertEquals(1, from.size());
		
		assertEquals("schema.PersonSource AS a", from.get(0).toString());
		
		
	}
	
	
//	public void testTabularJoin() throws Exception {
//		
//		test(
//			"src/test/resources/ShowlSqlTransformTest/tabular-join-transform", 
//			"http://example.com/ns/shape/PersonTargetShape");
//		
//		
//		
//	}

	


	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void load(String filePath) throws RDFParseException, RDFHandlerException, IOException {
		File sourceDir = new File(filePath);
		
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		URI parent = uri("http://example.com/ns/sys/EDW");
//		DatasourceIsPartOfFilter filter = new DatasourceIsPartOfFilter(parent);
		if (showlManager == null) {
			showlManager = new ShowlManager(shapeManager, reasoner);
		}
		showlManager.load();
		
	}

}

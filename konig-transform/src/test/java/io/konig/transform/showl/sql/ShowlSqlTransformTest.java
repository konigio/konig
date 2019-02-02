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

import java.io.File;
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
import io.konig.core.showl.ConsumesDataFromFilter;
import io.konig.core.showl.MappingStrategy;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.filters.DatasourceIsPartOfFilter;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.ValueExpression;

public class ShowlSqlTransformTest {

	private ShowlManager showlManager;
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private MappingStrategy strategy = new MappingStrategy(new ConsumesDataFromFilter(graph));
	private ShowlSqlTransform transform = new ShowlSqlTransform();
	
	private InsertStatement insert(String resourcePath, String shapeId) throws Exception {

		GcpShapeConfig.init();
		load(resourcePath);
		URI shapeIri = uri(shapeId);
		ShowlNodeShape node = showlManager.getNodeShape(shapeIri).findAny();

		strategy.selectMappings(node);
		
		return transform.createInsert(node, GoogleBigQueryTable.class);
	}
	
	@Ignore
	public void testTabular() throws Exception {
		
		InsertStatement insert = insert(
			"src/test/resources/ShowlSqlTransformTest/tabular", 
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
	
	@Test
	public void testTabularJoin() throws Exception {
		
		InsertStatement insert = insert(
			"src/test/resources/ShowlSqlTransformTest/tabular-join-transform", 
			"http://example.com/ns/shape/PersonTargetShape");
		
		
		
		System.out.println(insert.toString());
		
	}

	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void load(String filePath) throws RDFParseException, RDFHandlerException, IOException {
		File sourceDir = new File(filePath);
		OwlReasoner reasoner = new OwlReasoner(graph);
		ShapeManager shapeManager = new MemoryShapeManager();
		
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		URI parent = uri("http://example.com/ns/sys/EDW");
//		DatasourceIsPartOfFilter filter = new DatasourceIsPartOfFilter(parent);
		showlManager = new ShowlManager(shapeManager, reasoner);
		showlManager.load();
		
	}

}

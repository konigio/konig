package io.konig.schemagen.gcp;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.google.api.services.bigquery.model.ViewDefinition;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleBigQueryView;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.AndExpression;
import io.konig.sql.query.BooleanTerm;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.SqlFunctionExpression;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.NullPredicate;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.OrExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.SearchCondition;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SelectTableItemExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.TruthValue;
import io.konig.sql.query.WhereClause;

public class SimpleCurrentStateViewGeneratorTest {
	
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();
	
	private SimpleCurrentStateViewGenerator generator = new SimpleCurrentStateViewGenerator();

	@Test
	public void test() throws Throwable {
		
		load("SimpleCurrentStateViewGeneratorTest");
		URI shapeId = uri("http://example.com/shapes/PersonShape");
		Shape shape = shapeManager.getShapeById(shapeId);
		
		assertTrue(shape != null);
		
		GoogleBigQueryView datasource = shape.getShapeDataSource().stream()
				.filter(ds -> ds instanceof GoogleBigQueryView)
				.map(ds -> (GoogleBigQueryView) ds)
				.findFirst()
				.get();
		
		
		GoogleBigQueryTable fullTable = shape.getShapeDataSource().stream()
				.filter(ds -> ds.getClass() == GoogleBigQueryTable.class)
				.map(ds -> (GoogleBigQueryTable)ds)
				.findFirst()
				.get();
		
		SelectExpression select = generator.selectExpression(shape, fullTable);
		
		assertColumn(select, "A.id");
		assertColumn(select, "A.givenName");
		assertColumn(select, "A.modified");
		assertColumn(select, "A.deleted");
		
		assertTrue(select.getFrom().getTableItems().size() == 1);
		
		JoinExpression join = (JoinExpression) select.getFrom().getTableItems().get(0);
		
		assertTrue(join.getLeftTable() instanceof TableAliasExpression);
		
		TableAliasExpression left = (TableAliasExpression) join.getLeftTable();
		assertTrue(left.getTableName() instanceof TableNameExpression);
		assertEquals("schema.Person", left.getTableName().toString());
		assertEquals("A", left.getAlias());
		
		
		assertTrue(join.getRightTable() instanceof SelectTableItemExpression);
		SelectTableItemExpression right = (SelectTableItemExpression) join.getRightTable();
		
		SelectExpression rightSelect = right.getSelect();
		assertColumn(rightSelect, "id", "identifier");
		
		assertMaxModified(rightSelect);
		assertEquals(1, rightSelect.getFrom().getTableItems().size());
		TableItemExpression fromItem = rightSelect.getFrom().getTableItems().get(0);
		assertTrue(fromItem instanceof TableNameExpression);
		TableNameExpression fromItemName = (TableNameExpression) fromItem;
		assertEquals("schema.Person", fromItemName.getTableName());
		
		ViewDefinition viewDef = generator.createViewDefinition(shape, datasource);
		
		OnExpression joinOn = join.getJoinSpecification();
		assertTrue(joinOn != null);
		
		SearchCondition search = joinOn.getSearchCondition();
		assertTrue(search instanceof ComparisonPredicate);
		ComparisonPredicate compare = (ComparisonPredicate) search;
		assertEquals(ComparisonOperator.EQUALS, compare.getOperator());
		
		assertTrue(compare.getLeft() instanceof ColumnExpression);
		ColumnExpression compareLeft = (ColumnExpression) compare.getLeft();
		assertEquals("id", compareLeft.getColumnName());
		
		
		assertTrue(compare.getRight() instanceof ColumnExpression);
		ColumnExpression compareRight = (ColumnExpression) compare.getRight();
		assertEquals("identifier", compareRight.getColumnName());
		
		
		WhereClause where = select.getWhere();
		assertTrue(where != null);
		
		BooleanTerm term = where.getCondition();
		
		assertTrue(term instanceof AndExpression);
		AndExpression and = (AndExpression) term;
		assertEquals(2, and.getTermList().size());
		OrExpression deletedTerm = (OrExpression) and.getTermList().get(0);
		AndExpression deletedTermLeft = deletedTerm.getLeft();
		assertEquals(1, deletedTermLeft.getTermList().size());
		BooleanTerm deletedTermLeftBoolean = (BooleanTerm) deletedTermLeft.getTermList().get(0);
		assertTrue(deletedTermLeftBoolean instanceof NullPredicate);
		NullPredicate nullPredicate = (NullPredicate) deletedTermLeftBoolean;
		assertEquals(true, nullPredicate.isNull());
		assertTrue(nullPredicate.getValue() instanceof ColumnExpression);
		ColumnExpression nullCol = (ColumnExpression) nullPredicate.getValue();
		assertEquals("deleted", nullCol.getColumnName());
		
		BooleanTerm deletedTermRight = deletedTerm.getRight();
		assertTrue(deletedTermRight instanceof AndExpression);
		AndExpression deletedRightAnd = (AndExpression) deletedTermRight;
		assertEquals(1, deletedRightAnd.getTermList().size());
		
		ComparisonPredicate deletedTermCompare = (ComparisonPredicate) deletedRightAnd.getTermList().get(0);
		assertEquals(ComparisonOperator.EQUALS, deletedTermCompare.getOperator());
		
		assertTrue(deletedTermCompare.getLeft() instanceof ColumnExpression);
		ColumnExpression deletedCol = (ColumnExpression) deletedTermCompare.getLeft();
		assertEquals("deleted", deletedCol.getColumnName());
		
		TruthValue deletedTruth = (TruthValue) deletedTermCompare.getRight();
		assertEquals (TruthValue.FALSE, deletedTruth);
		
		assertTrue(viewDef != null);
		
		BooleanTerm where2 = and.getTermList().get(1);
		assertTrue(where2 instanceof ComparisonPredicate);
		
		ComparisonPredicate compareMax = (ComparisonPredicate) where2;
		assertEquals(ComparisonOperator.EQUALS, compareMax.getOperator());
		assertTrue(compareMax.getLeft() instanceof ColumnExpression);
		ColumnExpression compareMaxLeft = (ColumnExpression) compareMax.getLeft();
		assertEquals("modified", compareMaxLeft.getColumnName());
		assertTrue(compareMax.getRight() instanceof ColumnExpression);
		ColumnExpression compareMaxRight = (ColumnExpression) compareMax.getRight();
		assertEquals("maxModified", compareMaxRight.getColumnName());
		
		
		String schema = viewDef.getQuery();
		System.out.println(schema);
		
	}

	private void assertMaxModified(SelectExpression rightSelect) {
		

		AliasExpression alias = getAlias(rightSelect, "maxModified");
		QueryExpression e = alias.getExpression();
		assertTrue(e instanceof SqlFunctionExpression);
		SqlFunctionExpression func = (SqlFunctionExpression) e;
		assertEquals("MAX", func.getFunctionName());
		assertEquals(1, func.getArgList().size());
		
		QueryExpression arg = func.getArgList().get(0);
		assertTrue(arg instanceof ColumnExpression);
		ColumnExpression col = (ColumnExpression) arg;
		assertEquals("modified", col.getColumnName());
		
	}

	private AliasExpression getAlias(SelectExpression select, String aliasName) {
		
		return select.getValues().stream()
				.filter(v -> v instanceof AliasExpression)
				.map(v -> (AliasExpression) v)
				.filter(a -> aliasName.equals(a.getAlias()) )
				.findFirst()
				.get();
	}

	private void assertColumn(SelectExpression select, String columnName, String alias) {
		
		

		assertTrue(select.getValues().stream()
			.filter(v -> v instanceof AliasExpression)
			.map(v -> (AliasExpression) v)
			.filter(v -> v.getAlias().equals(alias))
			.map(v -> v.getExpression())
			.filter(v -> v instanceof ColumnExpression)
			.map(v -> (ColumnExpression)v)
			.filter(v -> v.toString().equals(columnName))
			.findFirst()
			.isPresent());
		
	}

	private void assertColumn(SelectExpression select, String columnName) {
		
		
		assertTrue(select.getValues().stream()
			.filter(v -> v instanceof ColumnExpression)
			.map(v -> (ColumnExpression) v)
			.filter(v -> columnName.equals(v.getColumnName()))
			.findFirst()
			.isPresent());
		
	}

	private URI uri(String value) {
	
		return new URIImpl(value);
	}

	private void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		GcpShapeConfig.init();
		File file = new File("src/test/resources", path);
		RdfUtil.loadTurtle(file, graph, shapeManager);
	}

}

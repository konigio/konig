package io.konig.transform.sql.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.FromExpression;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.SearchCondition;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.ValueContainer;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.factory.AbstractShapeRuleFactoryTest;
import io.konig.transform.rule.ShapeRule;

public class SqlFactoryTest extends AbstractShapeRuleFactoryTest {
	
	protected SqlFactory sqlFactory = new SqlFactory();
	
	@Before
	public void setUp() throws Exception {
		useBigQueryTransformStrategy();
	}
	
	@Test
	public void testJoinNestedEntity() throws Exception {
		
		load("src/test/resources/konig-transform/join-nested-entity");

		URI shapeId = iri("http://example.com/shapes/MemberShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		FromExpression from = select.getFrom();
		List<TableItemExpression> tableItems = from.getTableItems();
		assertEquals(1, tableItems.size());
		
		TableItemExpression tableItem = tableItems.get(0);
		assertTrue(tableItem instanceof JoinExpression);
		
		JoinExpression join = (JoinExpression) tableItem;
		
		TableItemExpression leftTable = join.getLeftTable();
		
		assertTrue(leftTable instanceof TableAliasExpression);
		
		TableAliasExpression leftTableAlias = (TableAliasExpression) leftTable;
		
		assertTrue(leftTableAlias.getTableName() instanceof TableNameExpression);
		TableNameExpression tne = (TableNameExpression) leftTableAlias.getTableName();
		
		assertEquals("schema.OriginPersonShape", tne.getTableName());
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(3, valueList.size());
		assertHasColumn(select, "a.id");
		assertHasColumn(select, "a.givenName");
		ValueExpression ve = assertHasColumn(select, null, "memberOf");
		StructExpression struct = struct(ve);
		
		assertHasColumn(struct, "b.id");
		assertHasColumn(struct, "b.name");
		
		OnExpression oe = join.getJoinSpecification();
		
		SearchCondition sc = oe.getSearchCondition();
		assertTrue(sc instanceof ComparisonPredicate);
		ComparisonPredicate cp = (ComparisonPredicate) sc;
		
		assertEquals(ComparisonOperator.EQUALS, cp.getOperator());
		ve = cp.getLeft();
		assertTrue(ve instanceof ColumnExpression);
		ColumnExpression ce = (ColumnExpression) ve;
		assertEquals("a.memberOf", ce.getColumnName());
		
		ve = cp.getRight();
		assertTrue(ve instanceof ColumnExpression);
		ce = (ColumnExpression) ve;
		assertEquals("b.id", ce.getColumnName());
	
		
	}
	
	private StructExpression struct(ValueExpression ve) {
		assertTrue(ve instanceof AliasExpression);
		AliasExpression ae = (AliasExpression) ve;
		QueryExpression qe = ae.getExpression();
		assertTrue(qe instanceof StructExpression);
		
		return (StructExpression) qe;
	}

	@Test
	public void testJoinById() throws Exception {
		
		load("src/test/resources/konig-transform/join-by-id");

		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		FromExpression from = select.getFrom();
		List<TableItemExpression> tableItems = from.getTableItems();
		assertEquals(1, tableItems.size());
		
		TableItemExpression tableItem = tableItems.get(0);
		assertTrue(tableItem instanceof JoinExpression);
		
		JoinExpression join = (JoinExpression) tableItem;
		
		TableItemExpression leftTable = join.getLeftTable();
		
		assertTrue(leftTable instanceof TableAliasExpression);
		
		TableAliasExpression leftTableAlias = (TableAliasExpression) leftTable;
		
		assertTrue(leftTableAlias.getTableName() instanceof TableNameExpression);
		TableNameExpression tne = (TableNameExpression) leftTableAlias.getTableName();
		
		assertEquals("schema.PersonNameShape", tne.getTableName());
		
		TableItemExpression rightTable = join.getRightTable();
		assertTrue(rightTable instanceof TableAliasExpression);
		
		TableAliasExpression rightTableAlias = (TableAliasExpression) rightTable;
		
		assertTrue(rightTableAlias.getTableName() instanceof TableNameExpression);
		tne = (TableNameExpression) rightTableAlias.getTableName();
		
		assertEquals("schema.PersonAlumniOfShape", tne.getTableName());
		
		List<ValueExpression> valueList = select.getValues();
		
		assertEquals(3, valueList.size());
		
		assertHasColumn(select, "a.id", null);
		assertHasColumn(select, "a.givenName", null);
		assertHasColumn(select, "b.alumniOf", null);
		
		
		
	}
	
	private ValueExpression assertHasColumn(ValueContainer container, String columnName) {
		return assertHasColumn(container, columnName, null);
	}
	
	private ValueExpression assertHasColumn(ValueContainer select, String columnName, String alias) {
		List<ValueExpression> valueList = select.getValues();
		for (ValueExpression ve : valueList) {
			if (ve instanceof AliasExpression) {
				AliasExpression ae = (AliasExpression) ve;
				if (ae.getAlias().equals(alias)) {
					
					if (columnName != null) {
						QueryExpression qe = ae.getExpression();
						assertTrue(qe instanceof ColumnExpression);
						ColumnExpression ce = (ColumnExpression) qe;
						assertEquals(columnName, ce.getColumnName());
					}
					
					return ve;
					
				}
			}
			if (ve instanceof ColumnExpression) {
				ColumnExpression ce = (ColumnExpression) ve;
				if (ce.getColumnName().equals(columnName)) {
					assertTrue(alias == null);
					return ve;
				}
				
			}
		}
		
		if (alias != null) {
			fail("Alias not found: " + alias);
		}
		
		fail("Column not found: " + columnName);
		return null;
		
	}

	@Test
	public void testFlattenedField() throws Exception {
		
		load("src/test/resources/konig-transform/flattened-field");

		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		FromExpression from = select.getFrom();
		List<TableItemExpression> tableItems = from.getTableItems();
		assertEquals(1, tableItems.size());
		
		TableItemExpression tableItem = tableItems.get(0);
		assertTrue(tableItem instanceof TableNameExpression);
		
		TableNameExpression tableName = (TableNameExpression) tableItem;
		assertEquals("schema.OriginPersonShape", tableName.getTableName());
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(1, valueList.size());
		
		ValueExpression value = valueList.get(0);
		assertTrue(value instanceof AliasExpression);
		
		AliasExpression aliasExpression = (AliasExpression) value;
		assertEquals("address", aliasExpression.getAlias());
		QueryExpression q = aliasExpression.getExpression();
		assertTrue(q instanceof StructExpression);
		
		StructExpression struct = (StructExpression) q;
		
		valueList = struct.getValues();
		assertEquals(1, valueList.size());
		
		value = valueList.get(0);
		assertTrue(value instanceof AliasExpression);
		
		aliasExpression = (AliasExpression) value;
		assertEquals("postalCode", aliasExpression.getAlias());
		
		q = aliasExpression.getExpression();
		assertTrue(q instanceof ColumnExpression);
		
		ColumnExpression column = (ColumnExpression) q;
		assertEquals("zipCode", column.getColumnName());
		
		
		
	}
	
	@Test
	public void testRenameFields() throws Exception {
		
		load("src/test/resources/konig-transform/rename-fields");

		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		FromExpression from = select.getFrom();
		List<TableItemExpression> tableItems = from.getTableItems();
		assertEquals(1, tableItems.size());
		
		TableItemExpression tableItem = tableItems.get(0);
		assertTrue(tableItem instanceof TableNameExpression);
		
		TableNameExpression tableName = (TableNameExpression) tableItem;
		assertEquals("schema.OriginPersonShape", tableName.getTableName());
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(1, valueList.size());
		
		ValueExpression value = valueList.get(0);
		assertTrue(value instanceof AliasExpression);
		
		AliasExpression aliasExpression = (AliasExpression) value;
		QueryExpression q = aliasExpression.getExpression();
		assertTrue(q instanceof ColumnExpression);
		
		
		
		ColumnExpression column = (ColumnExpression) q;
		assertEquals("first_name", column.getColumnName());
		assertEquals("givenName", aliasExpression.getAlias());
	}

	@Test
	public void testFieldExactMatch() throws Exception {
		
		load("src/test/resources/konig-transform/field-exact-match");

		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		FromExpression from = select.getFrom();
		List<TableItemExpression> tableItems = from.getTableItems();
		assertEquals(1, tableItems.size());
		
		TableItemExpression tableItem = tableItems.get(0);
		assertTrue(tableItem instanceof TableNameExpression);
		
		TableNameExpression tableName = (TableNameExpression) tableItem;
		assertEquals("schema.OriginPersonShape", tableName.getTableName());
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(1, valueList.size());
		
		ValueExpression value = valueList.get(0);
		assertTrue(value instanceof ColumnExpression);
		
		ColumnExpression column = (ColumnExpression) value;
		assertEquals("givenName", column.getColumnName());
	}


}

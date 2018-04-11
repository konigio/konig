package io.konig.transform.sql.factory;

/*
 * #%L
 * Konig Transform
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.formula.AdditiveOperator;
import io.konig.sql.query.AdditiveValueExpression;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.AndExpression;
import io.konig.sql.query.BooleanTerm;
import io.konig.sql.query.CastSpecification;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.CountStar;
import io.konig.sql.query.DateTimeUnitExpression;
import io.konig.sql.query.FromExpression;
import io.konig.sql.query.FunctionExpression;
import io.konig.sql.query.GroupByClause;
import io.konig.sql.query.GroupingElement;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.Result;
import io.konig.sql.query.SearchCondition;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SimpleCase;
import io.konig.sql.query.SimpleWhenClause;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.ValueContainer;
import io.konig.sql.query.ValueExpression;
import io.konig.sql.query.WhereClause;
import io.konig.transform.proto.AbstractShapeModelToShapeRuleTest;
import io.konig.transform.rule.ShapeRule;

public class SqlFactoryTest extends AbstractShapeModelToShapeRuleTest {
	
	protected SqlFactory sqlFactory = new SqlFactory();
	
	@Before
	public void setUp() throws Exception {
		useBigQueryTransformStrategy();
	}
	
	@Ignore
	public void testTransformInversePath() throws Exception {
		
		load("src/test/resources/konig-transform/inverse-property");

		URI shapeId = iri("http://example.com/shapes/ProductShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
	
		System.out.println(select.toString());
		
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

/*
SELECT
   CONCAT("http://example.com/product/", CAST(a.PRD_ID AS STRING)) AS id,
   ARRAY_AGG(STRUCT(
      "USD" AS priceCurrency,
      a.PRD_PRICE AS price
   ) AS offers),
   a.PRD_NAME AS name,
   a.PRD_ID AS originId,
   b.id AS category
FROM 
   schema.OriginProductShape AS a
 JOIN
   ex.ProductCategory AS b
 ON
   a.PRD_CAT=b.originId
 */
	@Ignore
	public void testBigQueryTransform() throws Exception {
		
		load("src/test/resources/konig-transform/bigquery-transform");

		URI shapeId = iri("http://example.com/shapes/BqProductShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(5, valueList.size());
		
		ValueExpression v = valueList.get(0);
		
		assertTrue(v instanceof AliasExpression);
		AliasExpression alias = (AliasExpression)v;
		
		assertEquals("id", alias.getAlias());
		
		QueryExpression q = alias.getExpression();
		assertTrue(q instanceof FunctionExpression);
		FunctionExpression f = (FunctionExpression) q;
		
		assertEquals("CONCAT", f.getFunctionName());
		List<QueryExpression> argList = f.getArgList();
		assertEquals(2, argList.size());
		
		q = argList.get(0);
		assertTrue(q instanceof StringLiteralExpression);
		StringLiteralExpression s = (StringLiteralExpression) q;
		assertEquals("http://example.com/product/", s.getValue());
		
		q = argList.get(1);
		assertTrue(q instanceof CastSpecification);
		CastSpecification c = (CastSpecification) q;
		
		v = c.getValue();
		assertTrue(v instanceof ColumnExpression);
		ColumnExpression column = (ColumnExpression) v;
		
		assertEquals("a.PRD_ID", column.getColumnName());
		String datatype = c.getDatatype();
		assertEquals("STRING", datatype);
		
		v = valueList.get(1);
		
		assertTrue(v instanceof FunctionExpression);
		f = (FunctionExpression) v;
		assertEquals("ARRAY_AGG", f.getFunctionName());
		
		assertEquals(1, f.getArgList().size());
		
		q = f.getArgList().get(0);
		assertTrue(q instanceof AliasExpression);
		alias = (AliasExpression) q;
		assertEquals("offers", alias.getAlias());
		
		q = alias.getExpression();
		assertTrue(q instanceof StructExpression);
		StructExpression struct = (StructExpression) q;
		List<ValueExpression> structValues = struct.getValues();
		assertEquals(2, structValues.size());
		
		v = structValues.get(0);
		assertTrue(v instanceof AliasExpression);
		alias = (AliasExpression) v;
		assertEquals("priceCurrency", alias.getAlias());
		
		q = alias.getExpression();
		assertTrue(q instanceof StringLiteralExpression);
		s = (StringLiteralExpression) q;
		
		assertEquals("USD", s.getValue());
		
		v = structValues.get(1);
		assertTrue(v instanceof AliasExpression);
		alias = (AliasExpression)v;
		assertEquals("price", alias.getAlias());
		q = alias.getExpression();
		assertTrue(q instanceof ColumnExpression);
		column = (ColumnExpression) q;
		assertEquals("a.PRD_PRICE", column.getColumnName());
		
		v = valueList.get(2);
		assertTrue(v instanceof AliasExpression);
		alias = (AliasExpression) v;
		assertEquals("name", alias.getAlias());
		
		q = alias.getExpression();
		assertTrue(q instanceof ColumnExpression);
		column = (ColumnExpression) q;
		assertEquals("a.PRD_NAME", column.getColumnName());
		
		
		v = valueList.get(3);
		assertTrue(v instanceof AliasExpression);
		alias = (AliasExpression) v;
		assertEquals("originId", alias.getAlias());
		
		q = alias.getExpression();
		assertTrue(q instanceof ColumnExpression);
		column = (ColumnExpression) q;
		assertEquals("a.PRD_ID", column.getColumnName());
		

		v = valueList.get(4);
		assertTrue(v instanceof AliasExpression);
		alias = (AliasExpression) v;
		assertEquals("category", alias.getAlias());
		
		q = alias.getExpression();
		assertTrue(q instanceof ColumnExpression);
		column = (ColumnExpression) q;
		assertEquals("b.id", column.getColumnName());
		
		
		
	}
	
/*
SELECT
   STRUCT(
      "Week" AS durationUnit,
      DATE_TRUNC(endTime, "Week") AS intervalStart
   ) AS timeInterval,
   COUNT(*) AS totalCount
FROM schema.BuyAction;
 */
	@Ignore
	public void testTimeInterval() throws Exception {
		
		load("src/test/resources/konig-transform/time-interval");

		URI shapeId = iri("http://example.com/shapes/SalesByCityShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(2, valueList.size());
		
		ValueExpression value = valueList.get(0);
		assertTrue(value instanceof AliasExpression);
		AliasExpression alias = (AliasExpression) value;
		assertEquals("timeInterval", alias.getAlias());
		
		QueryExpression query = alias.getExpression();
		assertTrue(query instanceof StructExpression);
		StructExpression struct = (StructExpression) query;
		
		List<ValueExpression> timeValueList = struct.getValues();
		assertEquals(2, timeValueList.size());
		
		value = timeValueList.get(0);
		assertTrue(value instanceof AliasExpression);
		alias = (AliasExpression)value;
		assertEquals("durationUnit", alias.getAlias());
		
		value = timeValueList.get(1);
		assertTrue(value instanceof AliasExpression);
		alias = (AliasExpression) value;
		assertEquals("intervalStart", alias.getAlias());
		
		query = alias.getExpression();
		assertTrue(query instanceof FunctionExpression);
		FunctionExpression func = (FunctionExpression) query;
		assertEquals("DATE_TRUNC", func.getFunctionName());
		List<QueryExpression> argList = func.getArgList();
		assertEquals(2, argList.size());
		
		query = argList.get(0);
		assertTrue(query instanceof ColumnExpression);
		ColumnExpression col = (ColumnExpression) query;
		assertEquals("endTime", col.getColumnName());
		
		query = argList.get(1);
		assertTrue(query instanceof StringLiteralExpression);
		StringLiteralExpression literal = (StringLiteralExpression) query;
		assertEquals("Week", literal.getValue());

		value = valueList.get(1);
		assertTrue(value instanceof AliasExpression);
		alias = (AliasExpression) value;
		assertEquals("totalCount", alias.getAlias());
		
		query = alias.getExpression();
		assertTrue(query instanceof CountStar);
		
		FromExpression from = select.getFrom();
		List<TableItemExpression> itemList = from.getTableItems();
		assertEquals(1, itemList.size());
		TableItemExpression item = itemList.get(0);
		assertTrue(item instanceof TableNameExpression);
		TableNameExpression tableName = (TableNameExpression) item;
		assertEquals("schema.BuyAction", tableName.getTableName());
		
		GroupByClause groupBy = select.getGroupBy();
		assertTrue(groupBy != null);
		
		List<GroupingElement> groupingList = groupBy.getElementList();
		assertEquals(1, groupingList.size());
		
		GroupingElement element = groupingList.get(0);
		assertTrue(element instanceof ColumnExpression);
		
		col = (ColumnExpression) element;
		assertEquals("timeInterval.intervalStart", col.getColumnName());
		
	}
/*
INSERT fact.SalesByCity (city, continent, country, state, timeInterval, totalCount)
SELECT
   STRUCT(
      e.id,
      e.containedInPlace,
      e.name,
      e.type
   ) AS city,
   STRUCT(
      c.id,
      c.containedInPlace,
      c.name,
      c.type
   ) AS continent,
   STRUCT(
      b.id,
      b.containedInPlace,
      b.name,
      b.type
   ) AS country,
   STRUCT(
      d.id,
      d.containedInPlace,
      d.name,
      d.type
   ) AS state,
   STRUCT(
      "Week" AS durationUnit,
      DATE_TRUNC(a.endTime, "Week") AS intervalStart
   ) AS timeInterval,
   COUNT(*) AS totalCount
FROM 
   schema.BuyAction AS a,
   UNNEST(a.location) AS b,
   UNNEST(a.location) AS c,
   UNNEST(a.location) AS d,
   UNNEST(a.location) AS e
WHERE b.type="Country"
   AND c.type="Continent"
   AND d.type="State"
   AND e.type="City"
GROUP BY e.id, timeInterval.intervalStart
 */
	@Ignore
	public void testAnalyticsModelInsert() throws Exception {
		
		load("src/test/resources/konig-transform/analytics-model");

		
		URI shapeId = iri("http://example.com/shapes/SalesByCityShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
	
		
		InsertStatement insert = sqlFactory.insertStatement(shapeRule);
		
		Map<String,String> whereMap = whereMap(insert);
		assertEquals(4, whereMap.size());
		assertTrue(whereMap.containsKey("city"));
		assertTrue(whereMap.containsKey("continent"));
		assertTrue(whereMap.containsKey("country"));
		assertTrue(whereMap.containsKey("state"));
		
		
		TableNameExpression tableName = insert.getTargetTable();
		assertEquals("fact.SalesByCity", tableName.getTableName());
		
		List<ColumnExpression> columnList = insert.getColumns();
		assertEquals(6, columnList.size());
		
		assertEquals("city", columnList.get(0).getColumnName());
		assertEquals("continent", columnList.get(1).getColumnName());
		assertEquals("country", columnList.get(2).getColumnName());
		assertEquals("state", columnList.get(3).getColumnName());
		assertEquals("timeInterval", columnList.get(4).getColumnName());
		assertEquals("totalCount", columnList.get(5).getColumnName());
		
		
		SelectExpression select = insert.getSelectQuery();
		List<ValueExpression> valueList = select.getValues();
		
		assertEquals(6, valueList.size());
		
		assertLocationStruct(valueList.get(0), "city", whereMap);
		assertLocationStruct(valueList.get(1), "continent", whereMap);
		assertLocationStruct(valueList.get(2), "country", whereMap);
		assertLocationStruct(valueList.get(3), "state", whereMap);
		
		ValueExpression v = valueList.get(4);
		assertTrue(v instanceof AliasExpression);
		AliasExpression a = (AliasExpression) v;
		assertEquals("timeInterval", a.getAlias());
		
		QueryExpression q = a.getExpression();
		assertTrue(q instanceof StructExpression);
		StructExpression s = (StructExpression) q;
		
		List<ValueExpression> sList = s.getValues();
		assertEquals(2, sList.size());
		v = sList.get(0);
		assertTrue(v instanceof AliasExpression);
		a = (AliasExpression) v;
		assertEquals("durationUnit", a.getAlias());
		q = a.getExpression();
		assertTrue(q instanceof StringLiteralExpression);
		StringLiteralExpression literal = (StringLiteralExpression) q;
		assertEquals("Week", literal.getValue());
		
		v = sList.get(1);
		assertTrue(v instanceof AliasExpression);
		a = (AliasExpression) v;
		assertEquals("intervalStart", a.getAlias());
		q = a.getExpression();
	
		assertTrue(q instanceof FunctionExpression);
		FunctionExpression f = (FunctionExpression) q;
		assertEquals("DATE_TRUNC", f.getFunctionName());
		List<QueryExpression> qList = f.getArgList();
		assertEquals(2, qList.size());
		
		q = qList.get(0);
		assertTrue(q instanceof ColumnExpression);
		ColumnExpression c = (ColumnExpression) q;
		assertEquals("a.endTime", c.getColumnName());
		
		q = qList.get(1);
		assertTrue(q instanceof StringLiteralExpression);
		literal = (StringLiteralExpression) q;
		assertEquals("Week", literal.getValue());
		
		v = valueList.get(5);
		assertTrue(v instanceof AliasExpression);
		a = (AliasExpression) v;
		
		assertEquals("totalCount", a.getAlias());
		q = a.getExpression();
		assertTrue(q instanceof CountStar);
		
		FromExpression from = select.getFrom();
		List<TableItemExpression> fList = from.getTableItems();
		assertEquals(1, fList.size());
		
		TableItemExpression t = fList.get(0);
		assertTrue(t instanceof JoinExpression);
		JoinExpression j = (JoinExpression) t;
		
		t = j.getLeftTable();
		assertTrue(t instanceof TableAliasExpression);
		TableAliasExpression ta = (TableAliasExpression) t;
		Set<String> fromSet = new HashSet<>();
		fromSet.add(ta.getAlias());
		
		t= assertUnnest(fromSet, j.getRightTable());
		t= assertUnnest(fromSet, t);
		t= assertUnnest(fromSet, t);
		t= assertUnnest(fromSet, t);
		
		assertTrue(fromSet.contains("a"));
		assertTrue(fromSet.contains("b"));
		assertTrue(fromSet.contains("c"));
		assertTrue(fromSet.contains("d"));
		assertTrue(fromSet.contains("e"));
		assertTrue(t == null);
		
		
		
		GroupByClause groupBy = select.getGroupBy();
		List<GroupingElement> gList = groupBy.getElementList();
		assertEquals(2, gList.size());
		
		GroupingElement g = gList.get(0);
		assertTrue(g instanceof ColumnExpression);
		c = (ColumnExpression) g;
		String cityTableAlias = whereMap.get("city");
		String cityId = cityTableAlias + ".id";
		assertEquals(cityId, c.getColumnName());
		
		g = gList.get(1);
		assertTrue(g instanceof ColumnExpression);
		c = (ColumnExpression) g;
		assertEquals("timeInterval.intervalStart", c.getColumnName());
		
	}
	
	private Map<String, String> whereMap(InsertStatement insert) {
		Map<String,String> map = new HashMap<>();
		
		WhereClause where = insert.getSelectQuery().getWhere();

		BooleanTerm condition = where.getCondition();
		assertTrue(condition instanceof AndExpression);
		AndExpression and = (AndExpression) condition;
		
		List<BooleanTerm> andList = and.getTermList();
		assertEquals(4, andList.size());
		
		
		for (BooleanTerm term : andList) {

			assertTrue(term instanceof ComparisonPredicate);
			ComparisonPredicate c = (ComparisonPredicate) term;
			ValueExpression v = c.getLeft();
			assertTrue(v instanceof ColumnExpression);
			ColumnExpression cn = (ColumnExpression) v;
			String value = cn.getColumnName();
			v = c.getRight();
			assertTrue(v instanceof StringLiteralExpression);
			StringLiteralExpression s = (StringLiteralExpression) v;
			
			String key = s.getValue().toLowerCase();
			int dot = value.indexOf('.');
			String type = value.substring(dot+1);
			assertEquals("type", type);
			value = value.substring(0, dot);
			map.put(key, value);
		}
		
		return map;
	}

	private TableItemExpression assertUnnest(Set<String> aliasSet, TableItemExpression e) {
	
		TableItemExpression right = null;
		if (e instanceof JoinExpression) {
			JoinExpression j = (JoinExpression) e;
			e = j.getLeftTable();
			right = j.getRightTable();
			
		}
		assertTrue(e instanceof TableAliasExpression);
		TableAliasExpression a = (TableAliasExpression) e;
		aliasSet.add(a.getAlias());
		
		e = a.getTableName();
		assertTrue(e instanceof FunctionExpression);
		FunctionExpression f = (FunctionExpression) e;
		assertEquals("UNNEST", f.getFunctionName());
		List<QueryExpression> argList = f.getArgList();
		assertEquals(1, argList.size());
		QueryExpression q = argList.get(0);
		assertTrue(q instanceof ColumnExpression);
		ColumnExpression c = (ColumnExpression) q;
		assertEquals("a.location", c.getColumnName());

		
		return right;
	
	}

	private void assertLocationStruct(ValueExpression v, String fieldName, Map<String,String> whereMap) {
		assertTrue(v instanceof AliasExpression);
		AliasExpression a = (AliasExpression) v;
		assertEquals(fieldName, a.getAlias());
		
		QueryExpression q = a.getExpression();
		assertTrue(q instanceof StructExpression);
		
		String alias = whereMap.get(fieldName);
		StructExpression s = (StructExpression) q;
		List<ValueExpression> list = s.getValues();
		assertColumn(list.get(0), alias, "id");
		assertColumn(list.get(1), alias, "containedInPlace");
		assertColumn(list.get(2), alias, "name");
		assertColumn(list.get(3), alias, "type");
		
		
	}

	private void assertColumn(ValueExpression v, String tableAlias, String fieldName) {
		assertTrue(v instanceof ColumnExpression);
		ColumnExpression c = (ColumnExpression) v;
		assertEquals(tableAlias + "." + fieldName, c.getColumnName());
		
	}

/*
SELECT
   ANY_VALUE(city),
   ANY_VALUE(continent),
   ANY_VALUE(country),
   ANY_VALUE(state),
   ANY_VALUE(STRUCT(
      DATE_TRUNC(timeInterval.intervalStart, Month) AS intervalStart
   )) AS timeInterval,
   COUNT(*) AS totalCount
FROM fact.SalesByCity
WHERE timeInterval.durationUnit="Week"
GROUP BY city.id, DATE_TRUNC(timeInterval.intervalStart, Month)
 */
	@Ignore
	public void testAnalyticsModel() throws Exception {
		
		load("src/test/resources/konig-transform/analytics-model");

		URI shapeId = iri("http://example.com/shapes/SalesByCityShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
	
		
		ShapeRule monthRollUp = shapeRule.getRollUp();
		assertTrue(monthRollUp != null);
		
		SelectExpression monthSelect = sqlFactory.selectExpression(monthRollUp);
		
		List<ValueExpression> valueList = monthSelect.getValues();
		assertEquals(6, valueList.size());
		
		assertColumn(valueList, 0, "city");
		assertColumn(valueList, 1, "continent");
		assertColumn(valueList, 2, "country");
		assertColumn(valueList, 3, "state");
		
		ValueExpression v = valueList.get(4);
		assertTrue(v instanceof AliasExpression);
		AliasExpression alias = (AliasExpression) v;
		assertEquals("timeInterval", alias.getAlias());
		
		QueryExpression q = alias.getExpression();
		assertTrue(q instanceof FunctionExpression);
		FunctionExpression func = (FunctionExpression) q;
		assertEquals("ANY_VALUE", func.getFunctionName());
		assertEquals(1, func.getArgList().size());
		QueryExpression query = func.getArgList().get(0);
		assertTrue(query instanceof StructExpression);
		StructExpression struct = (StructExpression) query;
		assertEquals(2, struct.getValues().size());
		
		v = struct.getValues().get(0);
		assertTrue(v instanceof AliasExpression);
		alias = (AliasExpression) v;
		assertEquals("durationUnit", alias.getAlias());
		
		q = alias.getExpression();
		assertTrue(q instanceof StringLiteralExpression);
		StringLiteralExpression s = (StringLiteralExpression)q;
		assertEquals("Month", s.getValue());
		
		v = struct.getValues().get(1);
		assertTrue(v instanceof AliasExpression);
		alias = (AliasExpression)v;
		assertEquals("intervalStart", alias.getAlias());
		q = alias.getExpression();
		assertTrue(q instanceof FunctionExpression);
		func = (FunctionExpression) q;
		assertDateTrunc(func);
		
		v = valueList.get(5);
		assertTrue(v instanceof AliasExpression);
		alias = (AliasExpression) v;
		assertEquals("totalCount", alias.getAlias());
		q = alias.getExpression();
		assertTrue(q instanceof CountStar);
		
		FromExpression f = monthSelect.getFrom();
		assertEquals(1,  f.getTableItems().size());
		
		TableItemExpression tableItem = f.getTableItems().get(0);
		assertTrue(tableItem instanceof TableNameExpression);
		TableNameExpression t = (TableNameExpression) tableItem;
		assertEquals("fact.SalesByCity", t.getTableName());
		
		WhereClause where = monthSelect.getWhere();
		BooleanTerm b = where.getCondition();
		assertTrue(b instanceof ComparisonPredicate);
		ComparisonPredicate cp = (ComparisonPredicate)b;
		assertEquals(ComparisonOperator.EQUALS, cp.getOperator());
		v = cp.getLeft();
		assertTrue(v instanceof ColumnExpression);
		ColumnExpression c = (ColumnExpression) v;
		assertEquals("timeInterval.durationUnit", c.getColumnName());
		
		v = cp.getRight();
		assertTrue(v instanceof StringLiteralExpression);
		s = (StringLiteralExpression) v;
		assertEquals("Week", s.getValue());
		
		GroupByClause g = monthSelect.getGroupBy();
		assertEquals(2, g.getElementList().size());
		
		GroupingElement ge = g.getElementList().get(0);
		assertTrue(ge instanceof ColumnExpression);
		c = (ColumnExpression) ge;
		assertEquals("city.id", c.getColumnName());
		
		ge = g.getElementList().get(1);
		assertTrue(ge instanceof FunctionExpression);
		func = (FunctionExpression) ge;
		assertDateTrunc(func);
		
		ShapeRule yearRollUp = monthRollUp.getRollUp();
		assertTrue(yearRollUp != null);
		
		SelectExpression yearSelect = sqlFactory.selectExpression(yearRollUp);
		assertTrue(yearSelect != null);
		
	}

	private void assertDateTrunc(FunctionExpression func) {

		assertEquals("DATE_TRUNC", func.getFunctionName());
		assertEquals(2, func.getArgList().size());
		QueryExpression q = func.getArgList().get(0);
		assertTrue(q instanceof ColumnExpression);
		ColumnExpression c = (ColumnExpression) q;
		assertEquals("timeInterval.intervalStart", c.getColumnName());
		q = func.getArgList().get(1);
		assertTrue(q instanceof DateTimeUnitExpression);
		DateTimeUnitExpression d = (DateTimeUnitExpression) q;
		assertEquals("Month", d.getValue());
	
	}

	private void assertColumn(List<ValueExpression> valueList, int i, String colName) {

		ValueExpression v1 = valueList.get(i);
		assertTrue(v1 instanceof FunctionExpression);
		FunctionExpression func = (FunctionExpression) v1;
		assertEquals("ANY_VALUE", func.getFunctionName());
		assertEquals(1, func.getArgList().size());
		QueryExpression funcArg = func.getArgList().get(0);
		assertTrue(funcArg instanceof ColumnExpression);
		ColumnExpression column = (ColumnExpression) funcArg;
		assertEquals(colName, column.getColumnName());
	}

	@Ignore
	public void testJoinById() throws Exception {
		
		load("src/test/resources/konig-transform/join-by-id");

		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		
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
		
		
		
		if ("schema.PersonNameShape".equals(tne.getTableName())) {
		
		
		
			TableItemExpression rightTable = join.getRightTable();
			assertTrue(rightTable instanceof TableAliasExpression);
			
			TableAliasExpression rightTableAlias = (TableAliasExpression) rightTable;
			
			assertTrue(rightTableAlias.getTableName() instanceof TableNameExpression);
			tne = (TableNameExpression) rightTableAlias.getTableName();
			
			assertEquals("schema.PersonAlumniOfShape", tne.getTableName());
			
			List<ValueExpression> valueList = select.getValues();
			
			assertEquals(3, valueList.size());
			
			assertColumn(select, "a.id", null);
			assertColumn(select, "a.givenName", null);
			assertColumn(select, "b.alumniOf", null);
		} else {
			assertEquals("schema.PersonAlumniOfShape", tne.getTableName());

			TableItemExpression rightTable = join.getRightTable();
			assertTrue(rightTable instanceof TableAliasExpression);
			
			TableAliasExpression rightTableAlias = (TableAliasExpression) rightTable;
			
			assertTrue(rightTableAlias.getTableName() instanceof TableNameExpression);
			tne = (TableNameExpression) rightTableAlias.getTableName();
			
			assertEquals("schema.PersonNameShape", tne.getTableName());
			
			List<ValueExpression> valueList = select.getValues();
			
			assertEquals(3, valueList.size());
			
			assertColumn(select, "a.id", null);
			assertColumn(select, "b.givenName", null);
			assertColumn(select, "a.alumniOf", null);
		}
		
		
		
	}

	@Ignore
	public void testCountStar() throws Exception {
		
		load("src/test/resources/konig-transform/count-star");

		URI shapeId = iri("http://example.com/shapes/SalesCountShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		ValueExpression value = select.getValues().get(0);
		assertTrue(value instanceof AliasExpression);
		AliasExpression alias = (AliasExpression) value;
		QueryExpression query = alias.getExpression();
		assertTrue(query instanceof CountStar);
		
		
		
	}
	
/*
SELECT
   actor,
   object,
   ARRAY_AGG(STRUCT(
      id AS id,
      STRUCT(
         endEvent.id,
         endEvent.actor,
         endEvent.endEventOf,
         endEvent.eventTime,
         endEvent.object,
         endEvent.type
      ) AS endEvent,
      STRUCT(
         startEvent.id,
         startEvent.actor,
         startEvent.eventTime,
         startEvent.object,
         startEvent.startEventOf,
         startEvent.type
      ) AS startEvent,
      actor,
      object
   ) AS subActivity)
FROM xas.AssessmentSession
GROUP BY actor, object
 */
	@Ignore
	public void testAssessmentEndeavor() throws Exception {
		load("src/test/resources/konig-transform/assessment-endeavor");

		URI shapeId = iri("http://schema.pearson.com/shapes/AssessmentEndeavorShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		List<ValueExpression> valueList = select.getValues();
		assertEquals(3, valueList.size());
		// TODO : Add more validation steps.
	}

	@Ignore
	public void testAssessmentSession() throws Exception {
		load("src/test/resources/konig-transform/assessment-session");

		URI shapeId = iri("http://schema.pearson.com/shapes/AssessmentSessionShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(3, valueList.size());
		ValueExpression id = valueList.get(0);
		assertTrue(id instanceof AliasExpression);
		AliasExpression idAlias = (AliasExpression) id;
		assertEquals("id", idAlias.getAlias());
		
		assertTrue(idAlias.getExpression() instanceof ColumnExpression);
		ColumnExpression idValue = (ColumnExpression) idAlias.getExpression();
		assertEquals("a.endEventOf", idValue.getColumnName());
		
		assertTrue(valueList.get(1) instanceof AliasExpression);
		AliasExpression endEvent = (AliasExpression) valueList.get(1);
		assertEquals("endEvent", endEvent.getAlias());
		assertTrue(endEvent.getExpression() instanceof StructExpression);
		
		assertTrue(valueList.get(2) instanceof AliasExpression);
		AliasExpression startEvent = (AliasExpression) valueList.get(2);
		assertEquals("startEvent", startEvent.getAlias());
		assertTrue(startEvent.getExpression() instanceof StructExpression);
		
		FromExpression from = select.getFrom();
		
		assertEquals(1, from.getTableItems().size());
		TableItemExpression tableItem = from.getTableItems().get(0);
		
		assertTrue(tableItem instanceof JoinExpression);
		
		JoinExpression join = (JoinExpression) tableItem;
		
		assertTrue(join.getLeftTable() instanceof TableAliasExpression);
		TableAliasExpression left = (TableAliasExpression)join.getLeftTable();
		
		assertTrue(left.getTableName() instanceof TableNameExpression);
		TableNameExpression leftName = (TableNameExpression) left.getTableName();
		assertEquals("{gcpProjectId}.xas.CompletedAssessment", leftName.getTableName());
		
		assertTrue(join.getRightTable() instanceof TableAliasExpression);
		TableAliasExpression right = (TableAliasExpression) join.getRightTable();
		
		assertTrue(right.getTableName() instanceof TableNameExpression);
		TableNameExpression rightName = (TableNameExpression) right.getTableName();
		assertEquals("{gcpProjectId}.xas.StartAssessment", rightName.getTableName());
		
		SearchCondition condition =  join.getJoinSpecification().getSearchCondition();
		
		assertTrue(condition instanceof ComparisonPredicate);
		ComparisonPredicate compare = (ComparisonPredicate) condition;
		assertTrue(compare.getLeft() instanceof ColumnExpression);
		
		ColumnExpression compareLeft = (ColumnExpression) compare.getLeft();
		assertEquals("b.startEventOf", compareLeft.getColumnName());

		assertTrue(compare.getRight() instanceof ColumnExpression);
		ColumnExpression compareRight = (ColumnExpression) compare.getRight();
		assertEquals("a.endEventOf", compareRight.getColumnName());
	}
/*
SELECT
   organization AS id,
   ARRAY_AGG(member) AS hasMember
FROM `{gcpProjectId}.org.Membership`
GROUP BY organization
 */
	@Ignore
	public void testBigQueryView() throws Exception {
		load("src/test/resources/konig-transform/bigquery-view");

		URI shapeId = iri("http://example.com/ns/shape/OrganizationShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		FromExpression from = select.getFrom();
		
		List<TableItemExpression> tableItemList = from.getTableItems();
		assertEquals(1, tableItemList.size());
		
		TableItemExpression item = tableItemList.get(0);
		
		assertTrue(item instanceof TableNameExpression);
		
		TableNameExpression tableName = (TableNameExpression) item;
		
		assertEquals(true, tableName.isWithQuotes());
		assertEquals("{gcpProjectId}.org.Membership", tableName.getTableName());
		
	
	}
	
/*
SELECT
   organization AS id,
   ARRAY_AGG(member) AS hasMember
FROM org.Membership
GROUP BY organization
 */
	@Ignore
	public void testArrayAgg() throws Exception {
		load("src/test/resources/konig-transform/array-agg");

		URI shapeId = iri("http://example.com/ns/shape/OrganizationShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(2, valueList.size());
		
		ValueExpression id = valueList.get(0);
		assertTrue(id instanceof AliasExpression);
		AliasExpression idAlias = (AliasExpression) id;
		
		QueryExpression idRoot = idAlias.getExpression();
		assertTrue(idRoot instanceof ColumnExpression);
		
		ColumnExpression idColumn = (ColumnExpression)idRoot;
		
		assertEquals("organization", idColumn.getColumnName());
		
		ValueExpression memberArray = valueList.get(1);
		assertTrue(memberArray instanceof AliasExpression);
		
		AliasExpression memberAlias = (AliasExpression) memberArray;
		assertEquals("hasMember", memberAlias.getAlias());
		
		QueryExpression memberRoot = memberAlias.getExpression();
		assertTrue(memberRoot instanceof FunctionExpression);
		
		FunctionExpression memberFunction = (FunctionExpression) memberRoot;
		assertEquals("ARRAY_AGG", memberFunction.getFunctionName());
		
		GroupByClause groupBy = select.getGroupBy();
		assertTrue(groupBy != null);
		List<GroupingElement> elementList = groupBy.getElementList();
		assertEquals(1, elementList.size());
		
		GroupingElement ge = elementList.get(0);
		assertTrue(ge instanceof ColumnExpression);
		
		ColumnExpression ce = (ColumnExpression) ge;
		assertEquals("organization", ce.getColumnName());
	}
	
	@Ignore
	public void testInjectModifiedTimestamp() throws Exception {
		
		load("src/test/resources/konig-transform/inject-modified-timestamp");

		URI shapeId = iri("http://example.com/shapes/PersonShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		List<ValueExpression> valueList = select.getValues();
		assertEquals(3, valueList.size());
		ValueExpression value = valueList.get(1);
		assertTrue(value instanceof AliasExpression);
		AliasExpression alias = (AliasExpression) value;
		QueryExpression qe = alias.getExpression();
		assertTrue(qe instanceof StringLiteralExpression);
		StringLiteralExpression sle = (StringLiteralExpression) qe;
		assertEquals("{modified}", sle.getValue());
		assertEquals("modified", alias.getAlias());
		
	}
	
/*
SELECT
   CONCAT("http://example.com/album/", CAST(a.album_id AS STRING)),
   STRUCT(
      CONCAT("http://example.com/artist/", CAST(b.group_id AS STRING)),
      b.group_name AS name
   ) AS byArtist,
   a.album_name AS name
FROM 
   schema.OriginMusicAlbumShape AS a
 JOIN
   schema.OriginMusicGroupShape AS b
 ON
   a.artist_id=b.group_id
 */
	@Ignore
	public void testGcpDeploy() throws Exception {
		
		load("src/test/resources/konig-transform/gcp-deploy");

		URI shapeId = iri("http://example.com/shapes/MusicAlbumShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(3, valueList.size());
		
		ValueExpression albumId = valueList.get(0);
		
		assertTrue(albumId instanceof AliasExpression);
		AliasExpression idAlias = (AliasExpression) albumId;
		assertTrue(idAlias.getExpression() instanceof FunctionExpression);
		FunctionExpression func = (FunctionExpression) idAlias.getExpression();
		assertEquals("CONCAT", func.getFunctionName());
		List<QueryExpression> argList = func.getArgList();
		assertEquals(2, argList.size());
		
		QueryExpression arg = argList.get(1);
		assertTrue(arg instanceof CastSpecification);
		CastSpecification cast = (CastSpecification) arg;
		assertEquals("STRING", cast.getDatatype());
		
		FromExpression from = select.getFrom();
		List<TableItemExpression> tableItems = from.getTableItems();
		assertEquals(1, tableItems.size());
		TableItemExpression tableItem = tableItems.get(0);
		assertTrue(tableItem instanceof JoinExpression);
		
		JoinExpression join = (JoinExpression) tableItem;
		OnExpression on = join.getJoinSpecification();
		SearchCondition search = on.getSearchCondition();
		assertTrue(search instanceof ComparisonPredicate);
		ComparisonPredicate compare = (ComparisonPredicate) search;
		ValueExpression left = compare.getLeft();
		ValueExpression right = compare.getRight();
		assertEquals("a.artist_id", left.toString());
		assertEquals("b.group_id", right.toString());
		
		
	}
	
	@Ignore
	public void testAggregateFunction() throws Exception {
		
		load("src/test/resources/konig-transform/aggregate-function");

		URI shapeId = iri("http://example.com/shapes/AssessmentMetricsShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		
		assertColumn(select, "resultOf", "assessment");
		QueryExpression qe = assertAlias(select, "avgScore").getExpression();
		assertTrue(qe instanceof FunctionExpression);
		FunctionExpression fe = (FunctionExpression) qe;
		assertEquals("AVG", fe.getFunctionName());
		
		List<QueryExpression> argList = fe.getArgList();
		assertEquals(1, argList.size());
		qe = argList.get(0);
		assertTrue(qe instanceof ColumnExpression);
		ColumnExpression ce = (ColumnExpression) qe;
		assertEquals("score", ce.getColumnName());
		
		GroupByClause groupBy = select.getGroupBy();
		assertTrue(groupBy != null);
		List<GroupingElement> groupingList = groupBy.getElementList();
		assertEquals(1, groupingList.size());
		GroupingElement ge = groupingList.get(0);
		assertTrue(ge instanceof ColumnExpression);
		ce = (ColumnExpression) ge;
		assertEquals("resultOf", ce.getColumnName());
	}
	
	@Ignore
	public void testDerivedProperty() throws Exception {
		
/*
 SELECT
   loss,
   profit - loss AS netIncome,
   profit
FROM ex.OriginAccountShape		
 */
		load("src/test/resources/konig-transform/derived-property");

		URI shapeId = iri("http://example.com/shapes/TargetAccountShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		AliasExpression ae = assertAlias(select, "netIncome");
		QueryExpression qe = ae.getExpression();
		assertTrue(qe instanceof AdditiveValueExpression);
		AdditiveValueExpression ave = (AdditiveValueExpression) qe;
		AdditiveOperator operator = ave.getOperator();
		assertEquals(AdditiveOperator.MINUS, operator);
		ValueExpression ve = ave.getLeft();
		assertTrue(ve instanceof ColumnExpression);
		ColumnExpression ce = (ColumnExpression) ve;
		assertEquals("profit", ce.getColumnName());
		ve = ave.getRight();
		assertTrue(ve instanceof ColumnExpression);
		ce = (ColumnExpression) ve;
		assertEquals("loss", ce.getColumnName());
	}
	

	
	public void testHasValueConstraint() throws Exception {
		
		// TODO : Enable this test!
		
		load("src/test/resources/konig-transform/has-value-constraint");

		URI shapeId = iri("http://example.com/shapes/BqProductShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(1, valueList.size());
		ValueExpression ve = valueList.get(0);
		assertTrue(ve instanceof AliasExpression);
		AliasExpression ae = (AliasExpression)ve;
		QueryExpression qe = ae.getExpression();
		assertTrue(qe instanceof StructExpression);
		StructExpression se = (StructExpression) qe;
		ae = (AliasExpression) assertAlias(se, "priceCurrency");
		
		qe = ae.getExpression();
		assertTrue(qe instanceof StringLiteralExpression);
		StringLiteralExpression sle = (StringLiteralExpression) qe;
		assertEquals("USD", sle.getValue());
		
		
	}
	
/*
SELECT
   b.id AS gender
FROM 
   schema.OriginPersonShape AS a
 JOIN
   schema.GenderType AS b
 ON
   a.gender=b.genderCode	
 */
	@Ignore
	public void testEnumField() throws Exception {
		
		load("src/test/resources/konig-transform/enum-field");

		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(1, valueList.size());
		
		ValueExpression ve = valueList.iterator().next();
		AliasExpression alias = (AliasExpression) ve;
		assertTrue(alias.getExpression() instanceof ColumnExpression);
		ColumnExpression sc = (ColumnExpression) alias.getExpression();
		assertEquals("b.id", sc.getColumnName());
		
		FromExpression from = select.getFrom();
		List<TableItemExpression> itemList = from.getTableItems();
		assertEquals(1, itemList.size());
		
		TableItemExpression item = itemList.get(0);
		
		assertTrue(item instanceof JoinExpression);
		
		JoinExpression join = (JoinExpression) item;
		assertTrue(join.getLeftTable() instanceof TableAliasExpression);
		TableAliasExpression leftAlias = (TableAliasExpression) join.getLeftTable();
		assertEquals("schema.OriginPersonShape", leftAlias.getTableName().toString());
		assertEquals("a", leftAlias.getAlias());
		
		assertTrue(join.getRightTable() instanceof TableAliasExpression);
		TableAliasExpression rightAlias = (TableAliasExpression) join.getRightTable();
		assertEquals("schema.GenderType", rightAlias.getTableName().toString());
		assertEquals("b", rightAlias.getAlias());
		
		SearchCondition condition = join.getJoinSpecification().getSearchCondition();
		assertTrue(condition instanceof ComparisonPredicate);
		assertEquals("a.gender=b.genderCode", condition.toString());
		
		
	}

	private void assertWhen(SimpleCase sc, String key, String value) {
		
		List<SimpleWhenClause> list = sc.getWhenClauseList();
		for (SimpleWhenClause clause : list) {
			ValueExpression whenOperand = clause.getWhenOperand();
			if (whenOperand instanceof StringLiteralExpression) {
				StringLiteralExpression sle = (StringLiteralExpression) whenOperand;
				if (key.equals(sle.getValue())) {
					Result result = clause.getResult();
					if (result instanceof StringLiteralExpression) {
						sle = (StringLiteralExpression) result;
						if (value.equals(sle.getValue())) {
							return;
						}
					}
				}
			}
		}
		
		fail("CASE not found: " + key + " => " + value);
		
	}

	@Ignore
	public void testJoinNestedEntityByPk() throws Exception {
		
		load("src/test/resources/konig-transform/join-nested-entity-by-pk");

		URI shapeId = iri("http://example.com/shapes/TargetMemberShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		List<ValueExpression> valueList = select.getValues();
		assertEquals(3, valueList.size());
		
		StructExpression memberOf = struct(assertAlias(select, "memberOf"));
		valueList = memberOf.getValues();
		assertEquals(2, valueList.size());
		assertColumnName(memberOf, "b.name");
		
		ValueExpression idValue = valueList.get(0);
		assertTrue(idValue instanceof AliasExpression);
		AliasExpression idAlias = (AliasExpression) idValue;
		
		assertTrue(idAlias.getExpression() instanceof FunctionExpression);
		FunctionExpression func = (FunctionExpression) idAlias.getExpression();
		assertEquals("CONCAT", func.getFunctionName());
		List<QueryExpression> argList = func.getArgList();
		assertEquals(2, argList.size());
		QueryExpression arg0 = argList.get(0);
		assertTrue(arg0 instanceof StringLiteralExpression);
		StringLiteralExpression literal = (StringLiteralExpression) arg0;
		assertEquals("http://example.com/org/", literal.getValue());
		QueryExpression arg1 = argList.get(1);
		assertTrue(arg1 instanceof ColumnExpression);
		ColumnExpression ce = (ColumnExpression) arg1;
		assertEquals("b.org_id", ce.getColumnName());
	}
	
	@Ignore
	public void testJoinNestedEntity() throws Exception {
		
		load("src/test/resources/konig-transform/join-nested-entity");

		URI shapeId = iri("http://example.com/shapes/MemberShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
		SelectExpression select = sqlFactory.selectExpression(shapeRule);
		
		// EXPECTED QUERY...
/*
			SELECT
			   a.id,
			   STRUCT(
			      b.id,
			      b.name
			   ) AS memberOf,
			   a.givenName
			FROM 
			   schema.OriginPersonShape AS a
			 JOIN
			   schema.OriginOrganizationShape AS b
			 ON
			   a.memberOf=b.id
		
 */
		
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
		assertColumnName(select, "a.id");
		assertColumnName(select, "a.givenName");
		ValueExpression ve = assertColumn(select, null, "memberOf");
		StructExpression struct = struct(ve);
		
		assertColumnName(struct, "b.id");
		assertColumnName(struct, "b.name");
		
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

	
	private AliasExpression assertAlias(ValueContainer container, String alias) {
		return (AliasExpression) assertColumn(container, null, alias);
	}
	
	private ValueExpression assertColumnName(ValueContainer container, String columnName) {
		return assertColumn(container, columnName, null);
	}
	
	private ValueExpression assertColumn(ValueContainer select, String columnName, String alias) {
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

	@Ignore
	public void testFlattenedField() throws Exception {
		
		load("src/test/resources/konig-transform/flattened-field");

		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
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

	@Ignore
	public void testFieldExactMatch() throws Exception {
		
		load("src/test/resources/konig-transform/field-exact-match");

		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule1(shapeId);
		
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

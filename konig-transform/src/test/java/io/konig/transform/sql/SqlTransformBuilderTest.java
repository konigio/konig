package io.konig.transform.sql;

/*
 * #%L
 * Konig Transform
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

import java.util.List;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.FromExpression;
import io.konig.sql.query.PathExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SqlFunctionExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.UpdateExpression;
import io.konig.sql.query.UpdateItem;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.model.TNodeShape;
import io.konig.transform.model.TransformModelBuilder;

public class SqlTransformBuilderTest {

	private MemoryGraph graph = new MemoryGraph(MemoryNamespaceManager.getDefaultInstance());
	private ShapeManager shapeManager = new MemoryShapeManager();
	private OwlReasoner owlReasoner = new OwlReasoner(graph);
	private TransformModelBuilder modelBuilder = new TransformModelBuilder(shapeManager, owlReasoner);
	private ShapeBuilder shapeBuilder = new ShapeBuilder(shapeManager);
	
	private SqlTransformBuilder sqlBuilder = new SqlTransformBuilder();
	
	@Test
	public void testIriTemplate() throws Exception {
		
		URI shapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		URI personId = uri("http://example.com/ns/PERSON_ID");
		shapeBuilder
			.beginShape(shapeId)
				.nodeKind(NodeKind.IRI)
				.targetClass(Schema.Person)
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/Person"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("Person")
				.endDataSource()
			.endShape()
			.beginShape(sourceShapeId)
				.targetClass(Schema.Person)
				.iriTemplate("http://example.com/person/{PERSON_ID}")
				.beginProperty(personId)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/SourcePerson"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("SourcePerson")
				.endDataSource()
			.endShape()
			;
		
		Shape shape = shapeManager.getShapeById(shapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		
		TNodeShape rootShape = modelBuilder.build(shape, ds);
		
		SqlTransform transform = new SqlTransform(rootShape);
		sqlBuilder.build(transform);
		
		System.out.println(transform.getInsert().toString());
		
		
	}
	@Ignore
	public void testUniqueKey() throws Exception {
		
		URI shapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		URI personId = uri("http://example.com/ns/PERSON_ID");
		shapeBuilder
			.beginShape(shapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginProperty(Konig.uid)
					.stereotype(Konig.uniqueKey)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/Person"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("Person")
				.endDataSource()
			.endShape()
			.beginShape(sourceShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginProperty(personId)
				.endProperty()
				.beginDerivedProperty(Konig.uid)
					.minCount(1)
					.maxCount(1)
					.beginFormula()
						.beginFunction("CONCAT")
							.literal("OneCrm-")
							.beginPath()
								.out(personId)
							.endPath()
						.endFunction()
					.endFormula()
				.endDerivedProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/SourcePerson"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("SourcePerson")
				.endDataSource()
			.endShape()
			;
		
		Shape shape = shapeManager.getShapeById(shapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		
		TNodeShape rootShape = modelBuilder.build(shape, ds);
		
		SqlTransform transform = new SqlTransform(rootShape);
		sqlBuilder.build(transform);
		
		SelectExpression select = transform.getInsert().getSelectQuery();
		List<ValueExpression> valueList = select.getValues();
		assertEquals(2, valueList.size());
		
		Optional<AliasExpression> alias = 
				valueList.stream()
				.filter(v -> v instanceof AliasExpression)
				.map(v -> (AliasExpression)v)
				.filter(v -> v.getAlias().equals("uid"))
				.findFirst();
		
		assertTrue(alias.isPresent());
		
		QueryExpression q = alias.get().getExpression();
		assertTrue(q instanceof SqlFunctionExpression);
		SqlFunctionExpression sfe = (SqlFunctionExpression) q;
		assertEquals("CONCAT", sfe.getFunctionName());
		assertEquals(2, sfe.getArgList().size());
		q = sfe.getArgList().get(0);
		assertTrue(q instanceof StringLiteralExpression);
		StringLiteralExpression sle = (StringLiteralExpression) q;
		assertEquals("OneCrm-", sle.getValue());
		
		q = sfe.getArgList().get(1);
		assertTrue(q instanceof ColumnExpression);
		ColumnExpression ce = (ColumnExpression) q;
		assertEquals("PERSON_ID", ce.getColumnName());
		
		UpdateExpression update = transform.getInsert().getUpdate();
		List<UpdateItem> itemList = update.getItemList();
		assertEquals(1, itemList.size());
		UpdateItem givenNameItem = itemList.get(0);
		PathExpression left = givenNameItem.getLeft();
		assertTrue(left instanceof ColumnExpression);
		ColumnExpression leftCol = (ColumnExpression) left;
		assertEquals("givenName", leftCol.getColumnName());
		
		
	}
	
	@Ignore
	public void testExactMatchDatatypeProperty() throws Exception {
		
		URI shapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		shapeBuilder
			.beginShape(shapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/Person"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("Person")
				.endDataSource()
			.endShape()
			.beginShape(sourceShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/SourcePerson"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("SourcePerson")
				.endDataSource()
			.endShape()
			;
		
		Shape shape = shapeManager.getShapeById(shapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		
		TNodeShape rootShape = modelBuilder.build(shape, ds);
		
		SqlTransform transform = new SqlTransform(rootShape);
		sqlBuilder.build(transform);
		
		SelectExpression select = transform.getSelectExpression();
		List<ValueExpression> values = select.getValues();
		
		assertEquals(1, values.size());
		ValueExpression ve = values.get(0);
		assertTrue(ve instanceof ColumnExpression);
		ColumnExpression column = (ColumnExpression) ve;
		assertEquals("givenName", column.getColumnName());
		
		FromExpression from = select.getFrom();
		assertEquals(1, from.getTableItems().size());
		TableItemExpression item = from.getTableItems().get(0);
		assertTrue(item instanceof TableNameExpression);
		TableNameExpression tableName = (TableNameExpression) item;
		assertEquals("SourcePerson", tableName.getTableName());
		
	}
	
	@Ignore
	public void testRenameDatatypeProperty() throws Exception {

		URI shapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		URI firstName = uri("http://example.com/ns/alias/first_name");
		shapeBuilder
			.beginShape(shapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.givenName)
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/Person"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("Person")
				.endDataSource()
			.endShape()
			.beginShape(sourceShapeId)
				.targetClass(Schema.Person)
				.beginProperty(firstName)
					.beginFormula()
						.beginPath()
							.out(Schema.givenName)
						.endPath()
					.endFormula()
				.endProperty()
				.beginDataSource(GoogleCloudSqlTable.Builder.class)
					.id(uri("http://example.com/database/SourcePerson"))
					.database("exampleDatabase")
					.instance("exampleInstance")
					.name("SourcePerson")
				.endDataSource()
			.endShape()
			;
		
		Shape shape = shapeManager.getShapeById(shapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		
		TNodeShape rootShape = modelBuilder.build(shape, ds);
		
		SqlTransform transform = new SqlTransform(rootShape);
		sqlBuilder.build(transform);
		
		SelectExpression select = transform.getSelectExpression();
		List<ValueExpression> values = select.getValues();
		
		assertEquals(1, values.size());
		ValueExpression ve = values.get(0);
		assertTrue(ve instanceof AliasExpression);
		AliasExpression alias = (AliasExpression) ve;
		assertEquals("givenName", alias.getAlias());
		assertTrue(alias.getExpression() instanceof ColumnExpression);
		ColumnExpression column = (ColumnExpression) alias.getExpression();
		assertEquals("first_name", column.getColumnName());
		
		FromExpression from = select.getFrom();
		assertEquals(1, from.getTableItems().size());
		TableItemExpression item = from.getTableItems().get(0);
		assertTrue(item instanceof TableNameExpression);
		TableNameExpression tableName = (TableNameExpression) item;
		assertEquals("SourcePerson", tableName.getTableName());
		
		System.out.println(transform.getInsert().toString());
	}



	private URI uri(String value) {
		return new URIImpl(value);
	}
}

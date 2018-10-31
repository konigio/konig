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
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.FromExpression;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.PathExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SimpleCase;
import io.konig.sql.query.SimpleWhenClause;
import io.konig.sql.query.SqlFunctionExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
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
	public void testNamedIndividualReference() throws Exception {

		URI sourceShapeId = uri("http://example.com/shapes/SourcePersonShape");
		URI targetShapeId = uri("http://example.com/shapes/TargetPersonShape");
		URI enumShapeId = uri("http://example.com/shapes/EnumShape");
		URI gender_code = uri("http://example.com/ns/gender_code");
		URI codeValue = uri("http://example.com/ns/codeValue");
		
		
		shapeBuilder
			.beginShape(sourceShapeId)
				.targetClass(Schema.Person)
				.nodeKind(NodeKind.IRI)
				.beginProperty(gender_code)
					.datatype(XMLSchema.STRING)
					.minCount(0)
					.maxCount(1)
					.beginFormula()
						.beginPath()
							.out(Schema.gender)
							.out(codeValue)
						.endPath()
					.endFormula()
				.endProperty()
				.beginDataSource(GoogleBigQueryTable.Builder.class)
					.id(uri("http://example.com/bq/SourcePerson"))
					.datasetId("exampleDataset")
					.tableId("SourcePerson")
				.endDataSource()
			.endShape()
			.beginShape(targetShapeId)
				.targetClass(Schema.Person)
				.nodeKind(NodeKind.IRI)
				.beginProperty(Schema.gender)
					.valueShape(enumShapeId)
					.valueClass(Schema.GenderType)
				.endProperty()
				.beginDataSource(GoogleBigQueryTable.Builder.class)
					.id(uri("http://example.com/bq/TargetPerson"))
					.datasetId("exampleDataset")
					.tableId("TargetPerson")
				.endDataSource()
			.endShape()
			.beginShape(enumShapeId)
				.targetClass(Schema.Enumeration)
				.nodeKind(NodeKind.IRI)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
			.endShape();

		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.Male, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Male, Schema.name, literal("Male Gender"));
		graph.edge(Schema.Male, codeValue, literal("M"));
		
		graph.edge(Schema.Female, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Female, Schema.name, literal("Female Gender"));
		graph.edge(Schema.Female, codeValue, literal("F"));

		Shape shape = shapeManager.getShapeById(targetShapeId);
		DataSource ds = shape.getShapeDataSource().get(0);
		
		TNodeShape tshape = modelBuilder.build(shape, ds);

		SqlTransform transform = new SqlTransform(tshape, new OwlReasoner(graph));
		sqlBuilder.build(transform);
		
		InsertStatement insert = transform.getInsert();
		assertTrue(insert.getColumns().stream().filter(c -> c.getColumnName().equals("gender")).findAny().isPresent());
		SelectExpression select = insert.getSelectQuery();
		Optional<AliasExpression> gender = 	select.getValues().stream()
				.filter(e -> e instanceof AliasExpression)
				.map(e -> (AliasExpression) e)
				.filter(a -> a.getAlias().equals("gender"))
				.findFirst();
		
		assertTrue(gender.isPresent());
		assertTrue(gender.get().getExpression() instanceof StructExpression);
		StructExpression struct = (StructExpression) gender.get().getExpression();
		
		SimpleCase idCase = caseExpression(struct, "id");
		assertTrue(idCase.getCaseOperand() instanceof ColumnExpression);
		ColumnExpression idCol = (ColumnExpression) idCase.getCaseOperand();
		assertEquals("gender_code", idCol.getColumnName());
		
		String male = whenClause(idCase, "M");
		assertEquals("Male", male);
		String female = whenClause(idCase, "F");
		assertEquals("Female", female);
		
		SimpleCase nameCase = caseExpression(struct, "name");
		assertEquals("Male Gender", whenClause(nameCase, "M"));
		assertEquals("Female Gender", whenClause(nameCase, "F"));
	}
	

	private String whenClause(SimpleCase idCase, String operand) {
		Optional<String> when = idCase.getWhenClauseList().stream()
				.filter(e -> e.getWhenOperand() instanceof StringLiteralExpression)
				.filter(e -> ((StringLiteralExpression)e.getWhenOperand()).getValue().equals(operand))
				.filter(e -> e.getResult() instanceof StringLiteralExpression)
				.map(e -> ((StringLiteralExpression) e.getResult()).getValue())
				.findFirst();
		assertTrue(when.isPresent());
		return when.get();
	}


	private SimpleCase caseExpression(StructExpression struct, String columnName) {
		
		Optional<SimpleCase> e = 
				struct.getValues().stream().filter(v -> v instanceof AliasExpression)
				.map(v -> (AliasExpression)v)
				.filter(v -> v.getAlias().equals(columnName))
				.filter(v -> v.getExpression() instanceof SimpleCase)
				.map(v -> (SimpleCase) v.getExpression())
				.findFirst();
		assertTrue(e.isPresent());
		return e.get();
	}


	private Value literal(String value) {
		return new LiteralImpl(value);
	}

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
		
		SqlTransform transform = new SqlTransform(rootShape, new OwlReasoner(new MemoryGraph()));
		sqlBuilder.build(transform);
		
		
		
	}
	@Test
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
		
		SqlTransform transform = new SqlTransform(rootShape, new OwlReasoner(new MemoryGraph()));
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
	
	@Test
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
		
		SqlTransform transform = new SqlTransform(rootShape, new OwlReasoner(new MemoryGraph()));
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
	
	@Test
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
		
		SqlTransform transform = new SqlTransform(rootShape, new OwlReasoner(new MemoryGraph()));
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
	}



	private URI uri(String value) {
		return new URIImpl(value);
	}
}

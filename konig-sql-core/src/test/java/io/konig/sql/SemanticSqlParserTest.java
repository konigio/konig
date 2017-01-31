package io.konig.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.io.NamespaceRDFHandler;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class SemanticSqlParserTest {
	private SQLSchemaManager schemaManager = new SQLSchemaManager();
	private ShapeManager shapeManager = new MemoryShapeManager();
	private NamespaceManager nsManager = new MemoryNamespaceManager();	
	private SemanticSqlParser sqlParser = new SemanticSqlParser(schemaManager, shapeManager, nsManager);
	
	@Before 
	public void setUp() throws Exception {
		sqlParser.setRDFHandler(new NamespaceRDFHandler(nsManager));
	}

	@Test
	public void testPrefixID() throws Exception {
		String text = "@prefix schema: <http://schema.org/> .";
		parse(text);
		
		assertEquals(Schema.NAMESPACE, sqlParser.getNamespaceMap().get("schema"));
	}
	
	@Test
	public void testOriginShapeDirective() throws Exception {
		String text = "@originShape <{foo}/bar>";
		
		parse(text);
		IriTemplate actual = sqlParser.getOriginShapeId();
		assertTrue(actual!=null);
		
		assertEquals("{foo}/bar", actual.toString());
		
	}

	
	@Test
	public void testTargetShapeDirective() throws Exception {
		String text = "@targetShape <{foo}/bar>";
		
		parse(text);
		IriTemplate actual = sqlParser.getTargetShapeId();
		assertTrue(actual!=null);
		
		assertEquals("{foo}/bar", actual.toString());
		
	}

	@Test
	public void testClassDirective() throws Exception {
		String text = "@class <{foo}/bar>";
		
		parse(text);
		IriTemplate actual = sqlParser.getClassId();
		assertTrue(actual!=null);
		
		assertEquals("{foo}/bar", actual.toString());
		
	}
	
	@Test
	public void testColumnNamespaceByPrefix() throws Exception {

		String text = 
			  "@prefix schema: <http://schema.org/> ."
			+ "@columnNamespace schema ";
		
		parse(text);
		
		assertEquals("http://schema.org/", sqlParser.getColumnNamespace());
	}
	

	@Test
	public void testColumnNamespaceByIRIREF() throws Exception {

		String text =  "@columnNamespace <http://schema.org/>";
		
		parse(text);
		
		assertEquals("http://schema.org/", sqlParser.getColumnNamespace());
	}
	
	@Test
	public void testTableId() throws Exception {
		String text = "CREATE TABLE foo.bar ( count BIGINT ) ";
		parse(text);
		
		SQLSchema schema = schemaManager.getSchemaByName("foo");
		assertTrue(schema!=null);
		
		SQLTableSchema table = schema.getTableByName("bar");
		assertTrue(table != null);
	}

	
	@Test
	public void testColumnType() throws Exception {
		
		String text =
			  "CREATE TABLE foo.bar (\n"
			+ "  first_name VARCHAR(32)\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "first_name");
		
		assertEquals(SQLDatatype.VARCHAR, column.getColumnType().getDatatype());
		assertEquals(new Integer(32), column.getColumnType().getSize());
		
	}
	
	@Test
	public void testNullConstraint() throws Exception {
		
		String text =
			  "CREATE TABLE foo.bar (\n"
			+ "  first_name VARCHAR(32) NULL\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "first_name");
		
		assertTrue(column.getNullConstraint() != null);
		
	}

	@Test
	public void testNamedNullConstraint() throws Exception {
		
		String text =
			  "CREATE TABLE foo.bar (\n"
			+ "  first_name VARCHAR(32) CONSTRAINT first_name_null NULL\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "first_name");
		
		assertTrue(column.getNullConstraint() != null);
		assertEquals("first_name_null", column.getNullConstraint().getName());
		
	}
	
	@Test
	public void testNotNullAndPrimaryKey() throws Exception {
		
		String text =
			  "CREATE TABLE foo.bar (\n"
			+ "  first_name VARCHAR(32) NOT NULL PRIMARY KEY\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "first_name");
		
		assertTrue(column.getNotNull() != null);
		
	}
	
	@Test
	public void testNotNullConstraint() throws Exception {
		
		String text =
			  "CREATE TABLE foo.bar (\n"
			+ "  first_name VARCHAR(32) NOT NULL\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "first_name");
		
		assertTrue(column.getNotNull() != null);
		
	}
	
	@Test
	public void testUniqueConstraint() throws Exception {
		
		String text =
			  "CREATE TABLE foo.bar (\n"
			+ "  first_name VARCHAR(32) UNIQUE\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "first_name");
		
		assertTrue(column.getUnique() != null);
		
	}
	
	@Test
	public void testPrimaryKeyConstraint() throws Exception {
		
		String text =
			  "CREATE TABLE foo.bar (\n"
			+ "  first_name VARCHAR(32) PRIMARY KEY\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "first_name");
		
		assertTrue(column.getPrimaryKey() != null);
		
	}

	@Test
	public void testForeignKey() throws Exception {
		
		String text =
			  "CREATE TABLE foo.Organization (\n"
			+ "  founder_id BIGINT,\n"
			+ "  FOREIGN KEY (founder_id) REFERENCES foo.Person (person_id)\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "Organization");
		List<SQLConstraint> constraintList = table.getConstraints();
		assertTrue(constraintList != null);
		assertEquals(1, constraintList.size());
		
		SQLConstraint c = constraintList.get(0);
		assertTrue(c instanceof ForeignKeyConstraint);
		ForeignKeyConstraint fk = (ForeignKeyConstraint) c;
		
		SQLColumnSchema source = fk.getSource().get(0);
		assertEquals("foo", source.getColumnTable().getSchema().getSchemaName());
		assertEquals("Organization", source.getColumnTable().getTableName());
		assertEquals("founder_id", source.getColumnName());
		
		SQLColumnSchema target = fk.getTarget().get(0);
		assertEquals("foo", target.getColumnTable().getSchema().getSchemaName());
		assertEquals("Person", target.getColumnTable().getTableName());
		assertEquals("person_id", target.getColumnName());
	}
	
	@Test
	public void testPrimaryKey() throws Exception {
		
		String text =
			  "CREATE TABLE foo.Organization (\n"
			+ "  org_id BIGINT,\n"
			+ "  PRIMARY KEY (org_id)\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "Organization");
		List<SQLConstraint> constraintList = table.getConstraints();
		assertTrue(constraintList != null);
		assertEquals(1, constraintList.size());
		
		SQLConstraint c = constraintList.get(0);
		assertTrue(c instanceof PrimaryKeyConstraint);
		PrimaryKeyConstraint pk = (PrimaryKeyConstraint) c;
		
		SQLColumnSchema column = pk.getColumnList().get(0);
		assertEquals(table.getColumnByName("org_id"), column);
		
		
		
	}
	@Test
	public void testTableUniqueConstraint() throws Exception {
		
		String text =
			  "CREATE TABLE foo.Organization (\n"
			+ "  org_id BIGINT,\n"
			+ "  UNIQUE (org_id)\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "Organization");
		List<SQLConstraint> constraintList = table.getConstraints();
		assertTrue(constraintList != null);
		assertEquals(1, constraintList.size());
		
		SQLConstraint c = constraintList.get(0);
		assertTrue(c instanceof UniqueConstraint);
		UniqueConstraint unique = (UniqueConstraint) c;
		
		SQLColumnSchema column = unique.getColumnList().get(0);
		assertEquals(table.getColumnByName("org_id"), column);
		
		
		
	}
	
	@Test
	public void testPrecision() throws Exception {
		
		String text =
			  "CREATE TABLE foo.bar (\n"
			+ "  speed DOUBLE(8,2)\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "speed");
		SQLColumnType type = column.getColumnType();
		assertTrue(type != null);
		assertEquals(new Integer(8), type.getSize());
		assertEquals(new Integer(2), type.getPrecision());
		
	}
	
	@Test
	public void testColumnNamespaceIriref() throws Exception {
		
		String text =
			  "CREATE TABLE foo.bar (\n"
			+ "  first_name VARCHAR(32)\n "
			+ "	   SEMANTICS NAMESPACE <http://example.com/alias/>\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "first_name");
		URI predicate = uri("http://example.com/alias/first_name");
		
		assertEquals(predicate, column.getColumnPredicate());
		
	}

	@Test
	public void testColumnNamespacePrefix() throws Exception {
		
		String text =
			  "@prefix alias: <http://example.com/alias/> ."
				
			+ "CREATE TABLE foo.bar (\n"
			+ "  first_name VARCHAR(32)\n "
			+ "	   SEMANTICS NAMESPACE alias\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "first_name");
		URI predicate = uri("http://example.com/alias/first_name");
		
		assertEquals(predicate, column.getColumnPredicate());
		
	}


	@Test
	public void testPath() throws Exception {
		
		String text =
			  "@prefix alias: <http://example.com/alias/> ."
			+ "@prefix schema: <http://schema.org/> ."
				
			+ "CREATE TABLE foo.bar (\n"
			+ "  first_name VARCHAR(32)\n "
			+ "	   SEMANTICS \n"
			+ "      namespace alias ;\n"
			+ "      path /schema:givenName\n"
			+ ")";
		
		parse(text);
		
		SQLTableSchema table = getTable("foo", "bar");
		SQLColumnSchema column = getColumn(table, "first_name");
		
		Path path = column.getEquivalentPath();
		
		assertEquals("/schema:givenName", path.toString(nsManager));
		
	}
	
	@Test
	public void testTableColumnNamespace() throws Exception {
		String text =
				  "@prefix alias: <http://example.com/alias/> ."
				+ "@prefix schema: <http://schema.org/> ."
					
				+ "CREATE TABLE foo.bar (\n"
				+ "  first_name VARCHAR(32)\n "
				+ ") SEMANTICS \n"
				+ "@columnNamespace alias .";
			
			parse(text);
			
			SQLTableSchema table = getTable("foo", "bar");

			
			assertEquals("http://example.com/alias/", table.getColumnNamespace());
			SQLColumnSchema column = table.getColumnByName("first_name");
			assertEquals(uri("http://example.com/alias/first_name"), column.getColumnPredicate());
	}
	
	@Test
	public void testTableClass() throws Exception {
		String text =
				  "@prefix alias: <http://example.com/alias/> ."
				+ "@prefix schema: <http://schema.org/> ."
					
				+ "CREATE TABLE foo.bar (\n"
				+ "  first_name VARCHAR(32)\n "
				+ ") SEMANTICS \n"
				+ "CLASS schema:Person .";
			
			parse(text);
			
			SQLTableSchema table = getTable("foo", "bar");
			assertEquals(Schema.Person, table.getTargetClass());
	}
	
	@Test
	public void testPhysicalShape() throws Exception {
		String text =
				  "@prefix alias: <http://example.com/alias/> .\n"
				+ "@prefix schema: <http://schema.org/> .\n"
				+ "@prefix shape: <http://example.com/shapes/> .\n"
					
				+ "CREATE TABLE foo.bar (\n"
				+ "  first_name VARCHAR(32)\n "
				+ ") SEMANTICS \n"
				+ "physical {\n"
				+ "  @id shape:PersonPhysicalShape"
				+ "} .";
			
			parse(text);
			
			SQLTableSchema table = getTable("foo", "bar");
			Shape shape = table.getPhysicalShape();
			assertTrue(shape != null);
			assertEquals(uri("http://example.com/shapes/PersonPhysicalShape"), shape.getId());
	}
	
	@Test
	public void testLogicalAndPhysicalShape() throws Exception {

		String text =
				  "@prefix alias: <http://example.com/alias/> .\n"
				+ "@prefix schema: <http://schema.org/> .\n"
				+ "@prefix shape: <http://example.com/shapes/> .\n"
				+ "@prefix konig: <http://www.konig.io/ns/core/> .\n"
					
				+ "CREATE TABLE foo.bar (\n"
				+ "  first_name VARCHAR(32)\n "
				+ ") SEMANTICS \n"
				+ "physical {\n"
				+ "  @id shape:PersonPhysicalShape\n"
				+ "} ;\n" 
				+ "logical {\n"
				+ "  @id shape:PersonLogicalShape ;"
				+ "  konig:shapeDataSource {"
				+ "    @id <urn:bigquery:com.example.person> "
				+ "  }"
				+ "} .";
			
			parse(text);
			
			SQLTableSchema table = getTable("foo", "bar");
			Shape shape = table.getLogicalShape();
			assertTrue(shape != null);
			assertEquals(uri("http://example.com/shapes/PersonLogicalShape"), shape.getId());
	}
	
	@Test
	public void testLogicalShape() throws Exception {
		String text =
				  "@prefix alias: <http://example.com/alias/> .\n"
				+ "@prefix schema: <http://schema.org/> .\n"
				+ "@prefix shape: <http://example.com/shapes/> .\n"
				+ "@prefix konig: <http://www.konig.io/ns/core/> .\n"
					
				+ "CREATE TABLE foo.bar (\n"
				+ "  first_name VARCHAR(32)\n "
				+ ") SEMANTICS \n"
				+ "logical {\n"
				+ "  @id shape:PersonLogicalShape ;"
				+ "  konig:shapeDataSource {"
				+ "    @id <urn:bigquery:com.example.person> "
				+ "  }"
				+ "} .";
			
			parse(text);
			
			SQLTableSchema table = getTable("foo", "bar");
			Shape shape = table.getLogicalShape();
			assertTrue(shape != null);
			assertEquals(uri("http://example.com/shapes/PersonLogicalShape"), shape.getId());
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	private SQLColumnSchema getColumn(SQLTableSchema table, String columnName) {
		SQLColumnSchema column = table.getColumnByName(columnName);
		assertTrue(column != null);
		return column;
	}

	private SQLTableSchema getTable(String schemaName, String tableName) {
		SQLSchema schema = schemaManager.getSchemaByName(schemaName);
		assertTrue(schema != null);
		
		SQLTableSchema table = schema.getTableByName(tableName);
		assertTrue(table != null);
		
		return table;
	}

	private void parse(String text) throws RDFParseException, RDFHandlerException, IOException {
		StringReader reader = new StringReader(text);
		sqlParser.parse(reader);
		
	}

	
}

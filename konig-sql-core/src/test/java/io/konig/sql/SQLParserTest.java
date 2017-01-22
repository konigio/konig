package io.konig.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.util.PathPattern;
import io.konig.core.vocab.Schema;

public class SQLParserTest {
	
	
	@Test
	public void testTableTargetShapeIriTemplate() throws Exception {
		String text = 
				  "@prefix schema : <http://schema.org/> . \n"
				+ "@prefix sh1: <http://example.com/shapes/v1/> . \n"
				+ "@tableTargetShapeIriTemplate <{sh1}{targetClassNamespacePrefix}/{targetClassLocalName}Shape> . \n"
				
				+ "CREATE TABLE registrar.Person ("
				+ "  name VARCHAR(255)"
				+ ")"
				+ "SEMANTICS targetClass schema:Person .";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		assertEquals(uri("http://example.com/shapes/v1/schema/PersonShape"), table.getTableTargetShapeId());
	}
	
	@Test
	public void testTableTargetShapeId() throws Exception {
		String text = 
				  "@prefix schema : <http://schema.org/> . "
				+ "@prefix sh1: <http://example.com/shapes/v1/schema/> . "
				
				+ "CREATE TABLE registrar.Person ("
				+ "  name VARCHAR(255)"
				+ ")"
				+ "SEMANTICS "
				+ "  targetShape sh1:PersonShape"
				+ ".";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		assertEquals(uri("http://example.com/shapes/v1/schema/PersonShape"), table.getTableTargetShapeId());
	}
	
	@Test
	public void testPathPattern() throws Exception {
		String text = 
				  "@prefix schema : <http://schema.org/> . "
				
				+ "CREATE TABLE registrar.Person ("
				+ "works_for_name VARCHAR(255),"
				+ "works_for_telephone VARCHAR(12)) "
				+ "SEMANTICS "
				+ "  pathPattern(works_for_, schema:Organization, /schema:worksFor)"
				+ ".";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		List<PathPattern> list = table.getPathPatternList();
		assertEquals(1, list.size());
		
		
		SQLColumnSchema workForName = table.getColumnByName("works_for_name");
		assertEquals("/schema:worksFor/schema:name", workForName.getEquivalentPath());
		
	}
	
	@Test
	public void testColumnPathTemplate() throws Exception {
		String text = 
				  "@prefix schema : <http://schema.org/> . "
				+ "@columnPathTemplate /schema:{columnNameCamelCase} ."
				
				+ "CREATE TABLE registrar.Person ("
				+ "given_name VARCHAR(255) NOT NULL)";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		SQLColumnSchema givenName = table.getColumnByName("given_name");
		String path = givenName.getEquivalentPath();
		assertEquals("/schema:givenName", path);
	}
	
	@Test
	public void testColumnPath() throws Exception {
		String text = 
				  "@prefix alias : <http://example.com/alias/> . "
				
				+ "CREATE TABLE registrar.Person ("
				+ "given_name VARCHAR(255) NOT NULL "
				+ "SEMANTICS path /alias:foo[alias:bar 1.0]"
				+ ")";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		SQLColumnSchema givenName = table.getColumnByName("given_name");
		String path = givenName.getEquivalentPath();
		assertEquals("/alias:foo[alias:bar 1.0]", path);
	}
	

	@Test
	public void testTableColumnNamespace() throws Exception {
		String text = 
				  "@prefix alias : <http://example.com/alias/> . "
				
				+ "CREATE TABLE registrar.Person ("
				+ "given_name VARCHAR(255) NOT NULL)"
				+ "SEMANTICS columnNamespace alias .";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		SQLColumnSchema givenName = table.getColumnByName("given_name");
		
		URI predicate = givenName.getColumnPredicate();
		assertTrue(predicate != null);
		assertEquals(uri("http://example.com/alias/given_name"), predicate);
	}

	
	
	@Test
	public void testColumnNamespaceDirective() throws Exception {
		String text = 
				  "@prefix alias : <http://example.com/ns/alias/> . "
				+ "@columnNamespace alias ."
				
				+ "CREATE TABLE registrar.Person ("
				+ "given_name VARCHAR(255) NOT NULL)";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		SQLColumnSchema givenName = table.getColumnByName("given_name");
		
		URI predicate = givenName.getColumnPredicate();
		assertTrue(predicate != null);
		assertEquals(uri("http://example.com/ns/alias/given_name"), predicate);
	}

	@Test
	public void testColumnNamespace() throws Exception {
		String text = 
				  "@prefix alias : <http://example.com/ns/alias/> ."
				
				+ "CREATE TABLE registrar.Person ("
				+ "given_name VARCHAR(255) NOT NULL SEMANTICS columnNamespace alias"
				+ ") ";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		SQLColumnSchema givenName = table.getColumnByName("given_name");
		
		URI predicate = givenName.getColumnPredicate();
		assertTrue(predicate != null);
		assertEquals(uri("http://example.com/ns/alias/given_name"), predicate);
	}
	
	

	@Test
	public void testTableTargetClassIriTemplate() throws Exception {
		String text = 
				  "@prefix schema : <http://schema.org/> . "
				+ "@tableTargetClassIriTemplate <{schema}{tableName}> ."
				
				+ "CREATE TABLE registrar.Person ("
				+ "name VARCHAR(255) NOT NULL)";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		URI targetClass = table.getTargetClass();
		assertTrue(targetClass != null);
		
		assertEquals(Schema.Person, targetClass);
	}
	
	@Test
	public void testTableShapeIriTemplate() throws Exception {
		String text = 
				"@prefix schema : <http://schema.org/> . \n"
				+ "@prefix shapeBaseURL : <http://example.com/shapes/v1/> .\n"
				+ "@tableShapeIriTemplate <{shapeBaseURL}{schemaName}/{tableNamePascalCase}Shape> .\n"
				
				+ "CREATE TABLE registrar.Person (\n"
				+ "name VARCHAR(255) NOT NULL)\n"
				+ "SEMANTICS targetClass schema:Person .\n";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		URI shapeId = table.getTableShapeId();
		assertTrue(shapeId != null);
		
		assertEquals(uri("http://example.com/shapes/v1/registrar/PersonShape"), shapeId);
	}
	
	@Test
	public void testTableTargetClass() throws Exception {
		String text = 
				"@prefix schema : <http://schema.org/> . "
				+ "CREATE TABLE registrar.Person ("
				+ "name VARCHAR(255) NOT NULL)"
				+ "SEMANTICS targetClass schema:Person .";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		URI targetClass = table.getTargetClass();
		assertTrue(targetClass != null);
		assertEquals(Schema.Person, targetClass);
	}

	@Test
	public void testTableShapeId() throws Exception {
		String text = 
				"@prefix shape1 : <http://example.com/shape/v1/> . "
				+ "CREATE TABLE registrar.Person ("
				+ "name VARCHAR(255) NOT NULL)"
				+ "SEMANTICS hasShape shape1:PersonShape .";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		URI shapeId = table.getTableShapeId();
		assertEquals(uri("http://example.com/shape/v1/PersonShape"), shapeId);
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	@Test
	public void testPrefixDirective() throws Exception {

		String text = 
				"@prefix schema : <http://schema.org/> . "
				+ "CREATE TABLE registrar.Person ("
				+ "name VARCHAR(255) NOT NULL)";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		assertTrue(table != null);
		
		NamespaceManager nsManager = table.getNamespaceManager();
		assertTrue(nsManager != null);
		
		Namespace schema = nsManager.findByPrefix("schema");
		assertTrue(schema != null);
		assertEquals("http://schema.org/", schema.getName());
		
		
	}
	
	@Test
	public void testColumnPrimaryKey() throws Exception {
		String text = "CREATE TABLE registrar.Person ("
				+ "taxID VARCHAR(255) NOT NULL PRIMARY KEY,"
				+ "name VARCHAR(255) NOT NULL)";
		
		SQLParser parser = new SQLParser();
		
		SQLTableSchema table  = parser.parseTable(text);
		
		SQLPrimaryKeyConstraint primaryKey = table.getPrimaryKeyConstraint();
		assertTrue(primaryKey != null);
		List<SQLColumnSchema> columnList = primaryKey.getColumnList();
		assertEquals(1, columnList.size());
		assertEquals("taxID", columnList.get(0).getColumnName());
		
	}
	
	@Test
	public void testTableForeignKeyAndShapeId() throws Exception {
		String text = 
				  "@prefix schema: <http://schema.org/> ."
				+ "@tableShapeIriTemplate <http://example.com/shapes/v1/schema/{targetClassLocalName}Shape> ."
				+ "CREATE TABLE registrar.Person ("
				+ "person_id BIGINT PRIMARY KEY NOT NULL,"
				+ "employer_id VARCHAR(255) NOT NULL,"
				+ "name VARCHAR(255), "
				+ "CONSTRAINT fk_employer FOREIGN KEY (employer_id) REFERENCES registrar.Organization (org_id)"
				+ ")"
				+ "SEMANTICS targetClass schema:Person .";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);

		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		
		URI shapeId = table.getTableShapeId();
		assertTrue(shapeId != null);
		assertEquals(uri("http://example.com/shapes/v1/schema/PersonShape"), shapeId);
		
		
	}
	
	@Test
	public void testTableForeignKey() throws Exception {
		String text = "CREATE TABLE registrar.Person ("
				+ "person_id BIGINT PRIMARY KEY NOT NULL,"
				+ "employer_id VARCHAR(255) NOT NULL,"
				+ "name VARCHAR(255), "
				+ "CONSTRAINT fk_employer FOREIGN KEY (employer_id) REFERENCES registrar.Organization (org_id)"
				+ ")";
		
		SQLParser parser = new SQLParser();
		
		SQLTableSchema table = parser.parseTable(text);
		List<SQLConstraint> constraintList = table.getConstraints();
		
		assertEquals(1, constraintList.size());
		
		assertTrue(constraintList.get(0) instanceof ForeignKeyConstraint);
		
		ForeignKeyConstraint fk = (ForeignKeyConstraint) constraintList.get(0);
		List<SQLColumnSchema> sourceList = fk.getSource();
		assertEquals(1, sourceList.size());
		
		assertEquals("employer_id", sourceList.get(0).getColumnName());
		List<SQLColumnSchema> targetList = fk.getTarget();
		
		SQLColumnSchema target = targetList.get(0);
		assertEquals("org_id", target.getColumnName());
		assertEquals("Organization", target.getColumnTable().getTableName());
		
		
	}
	
	@Test
	public void testTablePrimaryKey() throws Exception {
		String text = "CREATE TABLE registrar.Person ("
				+ "taxID VARCHAR(255) NOT NULL,"
				+ "name VARCHAR(255), "
				+ "CONSTRAINT pk_taxId PRIMARY KEY (taxID))";
		
		SQLParser parser = new SQLParser();
		
		SQLTableSchema table  = parser.parseTable(text);
		
		SQLPrimaryKeyConstraint primaryKey = table.getPrimaryKeyConstraint();
		assertTrue(primaryKey != null);
		List<SQLColumnSchema> columnList = primaryKey.getColumnList();
		assertEquals(1, columnList.size());
		assertEquals("taxID", columnList.get(0).getColumnName());
		
	}

	@Test
	public void test() throws Exception {
		String text = "CREATE TABLE registrar.Person ("
				+ "givenName VARCHAR(255) NOT NULL"
				+ ")";
		
		SQLParser parser = new SQLParser();
		
		SQLTableSchema table = parser.parseTable(text);
		assertTrue(table != null);
		assertEquals("Person", table.getTableName());
		SQLSchema schema = table.getSchema();
		assertTrue(schema!=null);
		assertEquals("registrar", schema.getSchemaName());
		
		SQLColumnSchema givenName = table.getColumnByName("givenName");
		assertTrue(givenName != null);
		assertEquals("givenName", givenName.getColumnName());
	
		SQLColumnType type = givenName.getColumnType();
		assertTrue(type != null);
		assertEquals(SQLDatatype.VARCHAR, type.getDatatype());
		
		assertEquals(new Integer(255), type.getSize());
		assertEquals(true, givenName.isNotNull());
		
		
	}

}

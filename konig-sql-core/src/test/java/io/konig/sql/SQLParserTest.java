package io.konig.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;
import io.konig.core.Path;
import io.konig.core.PathFactory;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Schema;

public class SQLParserTest {
	
	@Test
	public void testRegistrar() throws Exception {
		
		Reader reader = getReader("SQLParserTest/testRegistrar.sql");
		SQLParser parser = new SQLParser();
		
		parser.parseAll(reader);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Role");
		
		SQLColumnSchema column = table.getColumnByName("role_id");
		
		
		assertTrue(column!=null);
		assertTrue(column.getColumnType() != null);

		Path path = column.getEquivalentPath();
		assertTrue(path != null);
		assertEquals("/<http://example.com/ns/registrar/registrarId>", path.toString());
		
		IriTemplate template = table.getPhysicalShape().getIriTemplate();
		assertTrue(template != null);
		
		assertEquals("<http://example.com/role/{role_name}>", template.toString());
	}
	
	private Reader getReader(String resource) {
		
		return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
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
		PathFactory factory = new PathFactory(table.getNamespaceManager());
		
		Path actual = givenName.getEquivalentPath();
		Path expected = factory.createPath("/alias:foo[alias:bar 1.0]");
		
		assertEquals(expected, actual);
	}
	

	@Test
	public void testTableColumnNamespace() throws Exception {
		String text = 
				  "@prefix alias : <http://example.com/alias/> . "
				
				+ "CREATE TABLE registrar.Person ("
				+ "given_name VARCHAR(255) NOT NULL)"
				+ "SEMANTICS @columnNamespace alias .";
		
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
				+ "@columnNamespace alias "
				
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
				+ "given_name VARCHAR(255) NOT NULL SEMANTICS namespace alias"
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
	public void testTableTargetClass() throws Exception {
		String text = 
				"@prefix schema : <http://schema.org/> . "
				+ "CREATE TABLE registrar.Person ("
				+ "name VARCHAR(255) NOT NULL)"
				+ "SEMANTICS class schema:Person .";
		
		SQLParser parser = new SQLParser();
		
		parser.parseAll(text);
		
		SQLTableSchema table = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		URI targetClass = table.getTargetClass();
		assertTrue(targetClass != null);
		assertEquals(Schema.Person, targetClass);
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
		String text = "CREATE TABLE registrar.Person (\n"
				+ "taxID VARCHAR(255) NOT NULL PRIMARY KEY,\n"
				+ "name VARCHAR(255) NOT NULL)";
		
		SQLParser parser = new SQLParser();
		parser.parseAll(text);
		SQLTableSchema table  = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		
		PrimaryKeyConstraint primaryKey = table.getPrimaryKeyConstraint();
		assertTrue(primaryKey != null);
		List<SQLColumnSchema> columnList = primaryKey.getColumnList();
		assertEquals(1, columnList.size());
		assertEquals("taxID", columnList.get(0).getColumnName());
		
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

		parser.parseAll(text);
		SQLTableSchema table  = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
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

		parser.parseAll(text);
		SQLTableSchema table  = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
		
		PrimaryKeyConstraint primaryKey = table.getPrimaryKeyConstraint();
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

		parser.parseAll(text);
		SQLTableSchema table  = parser.getSchemaManager().getSchemaByName("registrar").getTableByName("Person");
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

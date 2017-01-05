package io.konig.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class SQLParserTest {
	

	
	@Test
	public void testColumnPrimaryKey() throws Exception {
		String text = "CREATE TABLE registrar.Person ("
				+ "taxID VARCHAR(255) NOT NULL PRIMARY KEY,"
				+ "name VARCHAR(255) NOT NULL);";
		
		SQLParser parser = new SQLParser();
		
		SQLTableSchema table  = parser.parseTable(text);
		
		SQLPrimaryKeyConstraint primaryKey = table.getPrimaryKeyConstraint();
		assertTrue(primaryKey != null);
		List<SQLColumnSchema> columnList = primaryKey.getColumnList();
		assertEquals(1, columnList.size());
		assertEquals("taxID", columnList.get(0).getColumnName());
		
	}
	
	@Test
	public void testTableForeignKey() throws Exception {
		String text = "CREATE TABLE registrar.Person ("
				+ "person_id BIGINT PRIMARY KEY NOT NULL,"
				+ "parent_id VARCHAR(255) NOT NULL,"
				+ "name VARCHAR(255), "
				+ "CONSTRAINT fk_parent FOREIGN KEY (parent_id) REFERENCES registrar.Person (person_id)"
				+ ");";
		
		SQLParser parser = new SQLParser();
		
		parser.parseTable(text);
		
		
	}
	
	@Test
	public void testTablePrimaryKey() throws Exception {
		String text = "CREATE TABLE registrar.Person ("
				+ "taxID VARCHAR(255) NOT NULL,"
				+ "name VARCHAR(255), "
				+ "CONSTRAINT pk_taxId PRIMARY KEY (taxID));";
		
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
				+ ");";
		
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

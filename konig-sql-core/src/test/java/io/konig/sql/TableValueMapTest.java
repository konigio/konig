package io.konig.sql;

import static org.junit.Assert.*;

import org.junit.Test;

public class TableValueMapTest {

	@Test
	public void test() {
		
		SQLTableSchema table = new SQLTableSchema();
		table.setTableName("foo_bar");
		
		TableValueMap map = new TableValueMap(table);
		assertEquals("foo_bar", map.get("tableName"));
		assertEquals("FooBar", map.get("tableNamePascalCase"));
		
	}

}

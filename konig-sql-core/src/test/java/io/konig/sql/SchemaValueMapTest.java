package io.konig.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.util.IriTemplate;

public class SchemaValueMapTest {

	@Test
	public void test() {
		
		SQLSchema schema = new SQLSchema();
		schema.setSchemaName("foo_bar");
		
		SchemaValueMap map = new SchemaValueMap(schema);
		assertEquals("foo_bar", map.get("schemaName"));
		assertEquals("FooBar", map.get("schemaNamePascalCase"));
	}
	
}

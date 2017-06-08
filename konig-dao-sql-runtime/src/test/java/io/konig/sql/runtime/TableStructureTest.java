package io.konig.sql.runtime;

import static org.junit.Assert.*;

import org.junit.Test;

public class TableStructureTest {

	@Test
	public void test() throws Exception  {
		
		TableStructure struct = new TableStructure();
		struct.addField("givenName");
		struct.addField("familyName");
		
		TableStructure addressStruct = new TableStructure();
		addressStruct.addField("streetAddress");
		addressStruct.addField("addressLocality");
		
		struct.addField("address", addressStruct);
		
		String actual = struct.toString();
		
		TableStructure loaded = JsonUtil.readString(TableStructure.class, actual);
		assertTrue(loaded != null);
		assertEquals(3, loaded.getFields().size());
		
		assertEquals("givenName", loaded.getFields().get(0).getName());
		assertEquals("familyName", loaded.getFields().get(1).getName());
		assertEquals("address", loaded.getFields().get(2).getName());
		
		struct = loaded.getFields().get(2).getStruct();
		assertEquals("streetAddress", struct.getFields().get(0).getName());
		assertEquals("addressLocality", struct.getFields().get(1).getName());
		
	}

}

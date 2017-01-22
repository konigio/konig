package io.konig.sql.query;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;

public class QueryWriterTest {

	@Test
	public void test() {
		
		StructExpression address = new StructExpression();
		address.add(new AliasExpression(new ColumnExpression("address_locality"), "addressLocality"));
		address.add(new AliasExpression(new ColumnExpression("address_region"), "addressRegion"));
		
		SelectExpression select = new SelectExpression();
		select.add(new AliasExpression(new ColumnExpression("given_name"), "givenName"));
		select.add(new AliasExpression(new ColumnExpression("family_name"), "familyName"));
		select.add(new AliasExpression(address, "address"));
		
		select.addFrom("registrar", "Person");
		
		StringWriter buffer = new StringWriter();
		
		QueryWriter writer = new QueryWriter(buffer);
		writer.print(select);
		String expected = 
				"SELECT\n" + 
				"   given_name AS givenName,\n" + 
				"   family_name AS familyName,\n" + 
				"   STRUCT(\n" + 
				"      address_locality AS addressLocality,\n" + 
				"      address_region AS addressRegion\n" + 
				"   ) AS address\n" + 
				"FROM registrar.Person;";
				
		
		String actual = buffer.toString().replace("\r", "");
		assertEquals(expected, actual);
	}

}

package io.konig.sql.query;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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

import java.io.StringWriter;

import org.junit.Test;

import io.konig.core.io.PrettyPrintWriter;

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
		
		select.getFrom().add(new TableNameExpression("registrar.Person"));
		
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter writer = new PrettyPrintWriter(buffer);
		select.print(writer);
		writer.close();
		String expected = 
				"SELECT\n" + 
				"   given_name AS givenName,\n" + 
				"   family_name AS familyName,\n" + 
				"   STRUCT(\n" + 
				"      address_locality AS addressLocality,\n" + 
				"      address_region AS addressRegion\n" + 
				"   ) AS address\n" + 
				"FROM registrar.Person";
				
		
		String actual = buffer.toString().replace("\r", "");
		assertEquals(expected, actual);
	}

}

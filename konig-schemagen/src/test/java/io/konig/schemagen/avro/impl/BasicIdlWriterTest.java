package io.konig.schemagen.avro.impl;

/*
 * #%L
 * Konig Schema Generator
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


import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

public class BasicIdlWriterTest {

	@Test
	public void test() throws Exception {
		StringWriter buffer = new StringWriter();
		
		BasicIdlWriter avro = new BasicIdlWriter(new PrintWriter(buffer));
		
		avro.writeImport("foo.avdl");

		avro.writeStartEnum("Gender");
		avro.writeSymbol("Male");
		avro.writeSymbol("Female");
		avro.writeEndEnum();
		
		avro.writeDocumentation("A person, alive, dead, undead or fictional");
		avro.writeNamespace("io.konig.shapes.v1.schema");
		avro.writeStartRecord("Person");
		avro.writeDocumentation("The person's given name.  In the US, this is commonly called\nthe last name.");
		avro.writeField("string", "givenName");
		avro.writeArrayField("Person", "parent");
		avro.writeStartUnion();
		avro.writeType("null");
		avro.writeType("Place");
		avro.writeType("ContactPoint");
		avro.writeEndUnion("workLocation");
		avro.writeEndRecord();
		
		
		avro.flush();
		String text = buffer.toString();
		
		System.out.println(text);
		
		assertTrue(text.contains("import idl \"foo.avdl\";"));
		assertTrue(text.contains(" * A person, alive, dead, undead or fictional"));
		assertTrue(text.contains("@namespace(\"io.konig.shapes.v1.schema\")"));
		assertTrue(text.contains("record Person {"));
		assertTrue(text.contains("  string givenName;"));
		assertTrue(text.contains("  array<Person> parent;"));
		assertTrue(text.contains("   * The person's given name.  In the US, this is commonly called"));
		assertTrue(text.contains("   * the last name."));
		assertTrue(text.contains("  union {null, Place, ContactPoint} workLocation;"));
		
	}

}

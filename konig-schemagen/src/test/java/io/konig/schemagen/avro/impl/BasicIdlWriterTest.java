package io.konig.schemagen.avro.impl;

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

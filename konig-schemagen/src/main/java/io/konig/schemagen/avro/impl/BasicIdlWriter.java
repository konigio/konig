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


import java.io.IOException;
import java.io.PrintWriter;

import io.konig.schemagen.avro.IdlWriter;

public class BasicIdlWriter implements IdlWriter {
	private PrintWriter out;
	private int indent=0;
	private String tab = "  ";
	private String enumComma;
	private String unionComma;
	private boolean newLine = false;

	public BasicIdlWriter(PrintWriter out) {
		this.out = out;
	}

	private void indent() throws IOException {
		for (int i=0; i<indent; i++) {
			out.write(tab);
		}
	}

	@Override
	public void writeDocumentation(String text) throws IOException {
		writeNewLine();
	
		text = text.replace("\r", "").replace("\t", tab);
		String[] lines = text.split("\\n");
		if (lines.length>0) {
			out.println();
			indent();
			out.println("/**");
			for (int i=0; i<lines.length; i++) {
				indent();
				out.print(" * ");
				out.println(lines[i]);
			}
			indent();
			out.println(" */");
		}
		
		
	}

	@Override
	public void writeImport(String fileName) throws IOException {
		
		out.print("import idl \"");
		out.print(fileName);
		out.println("\";");
		newLine = true;
		
	}

	@Override
	public void writeNamespace(String namespace) throws IOException {
		writeNewLine();
		indent();
		out.print("@namespace(\"");
		out.print(namespace);
		out.println("\")");
		
	}

	private void writeNewLine() {
		if (newLine) {
			out.println();
			newLine = false;
		}
		
	}

	@Override
	public void writeStartRecord(String name) throws IOException {
		writeNewLine();
		out.print("record ");
		out.print(name);
		out.println(" {");
		indent++;
	}

	@Override
	public void writeEndRecord() throws IOException {
		
		indent--;
		out.println("}");
		
	}

	@Override
	public void writeStartEnum(String name) throws IOException {
		
		out.print("enum ");
		out.print(name);
		out.print(" {");
		enumComma = "";
		indent++;
	}

	@Override
	public void writeSymbol(String symbol) throws IOException {
		out.println(enumComma);
		indent();
		out.print(symbol);
		enumComma = ",";
	}

	@Override
	public void writeEndEnum() throws IOException {
		out.println();
		indent--;
		out.println("}");
	}

	@Override
	public void writeField(String type, String name) throws IOException {
		indent();
		out.print(type);
		out.print(' ');
		out.print(name);
		out.println(";");
		
	}

	@Override
	public void writeArrayField(String itemType, String name) throws IOException {
		indent();
		out.print("array<");
		out.print(itemType);
		out.print("> ");
		out.print(name);
		out.println(";");
	}

	@Override
	public void writeStartUnion() throws IOException {
		
		indent();
		out.write("union {");
		unionComma = "";
		
	}

	@Override
	public void writeType(String type) throws IOException {
		if (unionComma != null) {
			out.write(unionComma);
			unionComma = ", ";
		}
		out.write(type);
	}

	@Override
	public void writeEndUnion(String fieldName) throws IOException {
		out.write("} ");
		out.write(fieldName);
		out.println(";");
		unionComma = null;
		
	}

	@Override
	public void flush() {
		out.flush();
	}

	@Override
	public void writeNull() throws IOException {
		writeType("null");		
	}

	@Override
	public void writeArray(String itemType) throws IOException {
		if (unionComma != null) {
			out.write(unionComma);
			unionComma = ", ";
		}

		out.print("array<");
		out.print(itemType);
		out.print(">");
		
	}

	@Override
	public void writeFieldName(String fieldName) throws IOException {
		out.print(' ');
		out.print(fieldName);
		out.println(';');
		
	}
	
}

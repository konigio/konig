package io.konig.schemagen.avro;

import java.io.IOException;

public interface IdlWriter {
	
	void writeDocumentation(String text) throws IOException;
	void writeImport(String fileName) throws IOException;
	void writeNamespace(String namespace) throws IOException;
	void writeStartRecord(String name) throws IOException;
	void writeEndRecord() throws IOException;
	void writeStartEnum(String name) throws IOException;
	void writeSymbol(String symbol) throws IOException;
	void writeEndEnum() throws IOException;
	
	void writeField(String type, String name) throws IOException;
	
	void writeArrayField(String itemType, String name) throws IOException;
	void writeArray(String itemType) throws IOException;
	
	void writeStartUnion() throws IOException;
	void writeType(String type) throws IOException;
	void writeNull() throws IOException;
	void writeEndUnion(String fieldName) throws IOException;
	
	void writeFieldName(String fieldName) throws IOException;
	
	void flush();
}

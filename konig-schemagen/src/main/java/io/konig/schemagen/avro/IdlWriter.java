package io.konig.schemagen.avro;

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

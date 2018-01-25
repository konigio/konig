package io.konig.schemagen.gcp;

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


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.CharacterEscapes;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.util.IOUtil;
import io.konig.datasource.DataSource;
import io.konig.gcp.common.ReplaceStringReader;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeHandler;

public class BigQueryLabelGenerator implements ShapeHandler {
	
	private static final String SCHEMA_FILE = "io/konig/schemagen/gcp/BigQueryFieldLabelSchema.json";
	
	private Graph graph;
	private File dataFile;
	private File schemaFile;

	private FileWriter writer;
	private int fieldCount;
	private JsonGenerator json;
	private Set<String> memory;
	private String datasetId;
	

	public BigQueryLabelGenerator(Graph graph, File schemaFile, File dataFile, String datasetName) {
		this.graph = graph;
		this.schemaFile = schemaFile;
		this.dataFile = dataFile;
		this.datasetId = datasetName;
	}

	@Override
	public void visit(Shape shape) {
		
		GoogleBigQueryTable table = getTable(shape);
		if (table != null) {
			String tableId = table.getTableIdentifier();
			visitProperties(tableId, shape);
		}
		

	}

	private void visitProperties(String tableId, Shape shape) {

		String tableKey = tableKey(tableId, shape);
		
		if (!memory.contains(tableKey)) {
			memory.add(tableKey);
			for (PropertyConstraint p : shape.getProperty()) {
				visitProperty(tableId, tableKey, p);
			}
		
		}
		
	}

	private String tableKey(String tableId, Shape shape) {
		Resource shapeId = shape.getId();
		if (shapeId == null) {
			throw new KonigException("Shape id must not be null for table: " + tableId);
		}
		StringBuilder builder = new StringBuilder();
		builder.append(tableId);
		builder.append('|');
		builder.append(shapeId.stringValue());
		return builder.toString();
	}

	private GoogleBigQueryTable getTable(Shape shape) {
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource ds : list) {
				if (ds instanceof GoogleBigQueryTable) {
					return (GoogleBigQueryTable) ds;
				}
			}
		}
		return null;
	}

	private void visitProperty(String tableId, String tableKey, PropertyConstraint p) {
		URI predicate = p.getPredicate();
		if (predicate != null) {
			Vertex v = graph.getVertex(predicate);
			if (v != null) {
				String fieldName = predicate.getLocalName();
				
				String fieldKey = tableKey + "|" + fieldName;
				if (!memory.contains(fieldKey)) {
					memory.add(fieldKey);
				
					Set<Value> labelSet = v.getValueSet(RDFS.LABEL);
					for (Value value : labelSet) {
						if (value instanceof Literal) {
							Literal literal = (Literal) value;
							
							String label = literal.getLabel();
							String language = literal.getLanguage();
							
							try {
								json.writeStartObject();
								json.writeStringField("tableName", tableId);
								json.writeStringField("fieldName", fieldName);
								json.writeStringField("label", label);
								json.writeStringField("language", language);
								json.writeEndObject();
								json.flush();
								writer.write("\n");
								fieldCount++;
							} catch (IOException e) {
								throw new KonigException(e);
							}
						}
					}
					
					Shape nested = p.getShape();
					if (nested != null) {
						visitProperties(tableId, nested);
					}
				}
			}
		}
		
	}

	@Override
	public void beginShapeTraversal() {
		try {
			fieldCount = 0;
			memory = new HashSet<>();
			dataFile.getParentFile().mkdirs();
			writer = new FileWriter(dataFile);
			JsonFactory factory = new JsonFactory();
			
			json = factory.createGenerator(writer);
			json.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
			
		} catch (IOException e) {
			throw new KonigException(e);
		}
		

	}

	@Override
	public void endShapeTraversal() {
		IOUtil.close(json, dataFile.getAbsolutePath());
		if (fieldCount > 0) {
			copySchema();
		} else {
			try {
				Files.deleteIfExists(Paths.get(dataFile.getAbsolutePath()));
			} catch (IOException e) {
				throw new KonigException(e);
			}
		}
		memory = null;
		json = null;
		writer = null;
	}

	private void copySchema() {
		
		schemaFile.getParentFile().mkdirs();
		try (
			InputStream rawInput = getClass().getClassLoader().getResourceAsStream(SCHEMA_FILE);
			InputStreamReader rawReader = new InputStreamReader(rawInput);
			ReplaceStringReader input = new ReplaceStringReader(rawReader, "${datasetId}", datasetId);
		) {
			try (FileWriter out = new FileWriter(schemaFile)) {
				char[] data = new char[1024];
				int len;
				while ((len=input.read(data)) != -1) {
					out.write(data, 0, len);
				}
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	

}

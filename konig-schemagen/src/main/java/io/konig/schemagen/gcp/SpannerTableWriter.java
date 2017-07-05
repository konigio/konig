package io.konig.schemagen.gcp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;

import io.konig.core.KonigException;
import io.konig.core.util.IOUtil;
import io.konig.gcp.datasource.SpannerTableReference;
import io.konig.shacl.Shape;

public class SpannerTableWriter implements SpannerTableVisitor {
	
	private File baseDir;

	public SpannerTableWriter(File baseDir) {
		this.baseDir = baseDir;
		baseDir.mkdirs();
	}

	@Override
	public void visit(SpannerTable table) {
		
		File tableFile = tableFile(table);
		JacksonFactory factory = JacksonFactory.getDefaultInstance();

		FileWriter writer = null;
		try {
			writer = new FileWriter(tableFile);

			JsonGenerator generator = factory.createJsonGenerator(writer);
			generator.enablePrettyPrint();
			generator.writeStartArray();
			generator.writeString(writeTableDefinition(table));
			generator.writeEndArray();
			generator.flush();
		
		} catch (IOException e) {
			throw new KonigException(e);
		} finally {
			IOUtil.close(writer, tableFile.getName());
		}
		
	}

	private File tableFile(SpannerTable table) {
		
		SpannerTableReference tableRef = table.getTableReference();
		if (tableRef == null) {
			throw new KonigException("Table reference");
		}
		String tableId = tableRef.getTableName();
		if (tableId == null) {
			throw new KonigException("tableId is not defined");
		}
		String datasetId = tableRef.getDatabaseName();
		if (datasetId == null) {
			throw new KonigException("Database Id is not defined for table: " + tableId);
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(datasetId);
		builder.append('.');
		builder.append(tableId);
		builder.append(".json");
		
		return new File(baseDir, builder.toString());
	}
	

	private String writeTableDefinition(SpannerTable table) {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE ");
		builder.append(table.getTableReference().getTableName());
		builder.append("(");
		
		builder.append(writeFieldDefinition(table));
		
		builder.append(")");
		return builder.toString();
	}
	
	private String writeFieldDefinition(SpannerTable table) {

		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (SpannerTable.Field field : table.getFields()) {
			if (i > 0) builder.append(", ");
			builder.append(field.getFieldName());
			builder.append(" ");
			builder.append(field.getFieldType() + "(1024)");
			FieldMode mode = field.getFieldMode();
			if (mode == FieldMode.REQUIRED) {
				
				builder.append(" ");
				builder.append("NOT NULL");
			}
			++i;
		}
		
		return builder.toString();
	}

}

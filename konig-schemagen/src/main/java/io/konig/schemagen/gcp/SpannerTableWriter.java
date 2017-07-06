package io.konig.schemagen.gcp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;

import io.konig.core.KonigException;
import io.konig.core.util.IOUtil;
import io.konig.gcp.datasource.SpannerTableReference;

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
			generator.writeString(writeTableDefinition(table));
			//generator.writeEndArray();
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
		String tableName = tableRef.getTableName();
		if (tableName == null) {
			throw new KonigException("tableName is not defined");
		}
		String databaseName = tableRef.getDatabaseName();
		if (databaseName == null) {
			throw new KonigException("Database Id is not defined for table: " + tableName);
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(databaseName);
		builder.append('.');
		builder.append(tableName);
		builder.append(".json");
		
		return new File(baseDir, builder.toString());
	}
	

	private String writeTableDefinition(SpannerTable table) {
		StringBuilder builder = new StringBuilder();
		builder.append("CREATE TABLE ");
		builder.append(table.getTableReference().getTableName());
		builder.append(" (").append(System.getProperty("line.separator"));
		
		builder.append(writeFieldDefinition(table));
		
		builder.append(");");
		return builder.toString();
	}
	
	private String writeFieldDefinition(SpannerTable table) {

		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (SpannerTable.Field field : table.getFields()) {
			if (i > 0) builder.append(",\n");
			
			builder.append("\t").append(field.getFieldName()).append(" ");
			
			if (field.getFieldType() == SpannerDatatype.ARRAY) {
				builder.append(SpannerDatatype.ARRAY.toString()).append("<");
			}
			
			builder.append(field.getFieldType());
			
			if (field.getFieldType() == SpannerDatatype.STRING) {
				builder.append("(MAX)");
			}
				
				
			if (field.getFieldType() == SpannerDatatype.ARRAY) {
				builder.append(">");
			}
			
			FieldMode mode = field.getFieldMode();
			if (mode == FieldMode.REQUIRED) {
				builder.append(" NOT NULL");
			}
			++i;
		}
		
		return builder.toString();
	}

}

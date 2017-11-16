package com.google.cloud.bigquery;

import java.util.ArrayList;
import java.util.List;

import com.google.api.services.bigquery.model.ExternalDataConfiguration;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.cloud.bigquery.Field.Mode;
import com.google.cloud.bigquery.Field.Type;

/*
 * #%L
 * Konig GCP Common
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

public class KonigBigQueryUtil {

	public static DatasetInfo createDatasetInfo(com.google.api.services.bigquery.model.Dataset model) {
		return new DatasetInfo.BuilderImpl(model).build();
	}

	public static TableInfo createTableInfo(com.google.api.services.bigquery.model.Table model) {
		String type = model.getType();
		if (type == null) {
			model.setType(TableDefinition.Type.TABLE.name());
		}

		TableId tableId = TableId.fromPb(model.getTableReference());
		TableDefinition definition = tableDefinition(model);
		return TableInfo.newBuilder(tableId, definition).build();

	}

	private static TableDefinition tableDefinition(Table model) {
		ExternalDataConfiguration external = model.getExternalDataConfiguration();

		Schema schema = schema(model.getSchema());
		if (external != null) {
			return ExternalTableDefinition.of(external.getSourceUris(), schema, formatOptions(external));
		}
		return StandardTableDefinition.of(schema);
	}

	private static FormatOptions formatOptions(ExternalDataConfiguration external) {
	
		String sourceFormat = external.getSourceFormat().toUpperCase();
		switch (sourceFormat) {
		case "CSV" : return csvOptions(external);
		case "JSON" : return FormatOptions.json();
		
		}
		throw new RuntimeException("invalid format: " + sourceFormat);
	}

	private static FormatOptions csvOptions(ExternalDataConfiguration external) {

		com.google.api.services.bigquery.model.CsvOptions options = external.getCsvOptions();
		if (options == null) {
			return FormatOptions.csv();
		}
		
		CsvOptions.Builder builder = CsvOptions.newBuilder();
		
		if (options.getAllowJaggedRows() != null) {
			builder.setAllowJaggedRows(options.getAllowJaggedRows());
		}
		
		if (options.getAllowQuotedNewlines() != null) {
			builder.setAllowQuotedNewLines(options.getAllowQuotedNewlines());
		}
		if (options.getSkipLeadingRows() != null) {
			builder.setSkipLeadingRows(options.getSkipLeadingRows());
		}
		
		builder.setEncoding(options.getEncoding());
		builder.setFieldDelimiter(options.getFieldDelimiter());
		builder.setQuote(options.getQuote());
		
		
		return builder.build();
	}

	private static Schema schema(TableSchema schema) {
		Schema.Builder builder = Schema.newBuilder();

		for (TableFieldSchema field : schema.getFields()) {
			builder.addField(field(field));
		}

		return builder.build();
	}

	private static Field field(TableFieldSchema field) {

		return Field.newBuilder(field.getName(), fieldType(field)).setMode(mode(field)).build();
	}

	private static Mode mode(TableFieldSchema field) {
		switch (field.getMode()) {
		case "REQUIRED":
			return Field.Mode.REQUIRED;
		case "REPEATED":
			return Field.Mode.REPEATED;
		case "NULLABLE":
			return Field.Mode.NULLABLE;
		}
		throw new RuntimeException("Invalid mode: " + field.getMode());
	}

	private static Type fieldType(TableFieldSchema field) {
		String type = field.getType();
		type = type.toUpperCase();
		switch (type) {
		case "STRING":
			return Field.Type.string();
		case "INT64":
			return Field.Type.integer();
		case "FLOAT64":
			return Field.Type.floatingPoint();
		case "BOOL":
			return Field.Type.bool();
		case "BYTES":
			return Field.Type.bytes();
		case "DATE":
			return Field.Type.date();
		case "DATETIME":
			return Field.Type.datetime();
		case "TIME":
			return Field.Type.time();
		case "TIMESTAMP":
			return Field.Type.timestamp();
		case "RECORD":
			return Field.Type.record(fieldList(field));

		}
		throw new RuntimeException("Invalid type: " + type);
	}

	private static List<Field> fieldList(TableFieldSchema record) {

		List<Field> fieldList = new ArrayList<>();
		for (TableFieldSchema field : record.getFields()) {
			fieldList.add(field(field));
		}
		return fieldList;
	}

}

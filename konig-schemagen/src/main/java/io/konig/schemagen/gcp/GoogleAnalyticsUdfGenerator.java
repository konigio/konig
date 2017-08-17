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
import java.io.FileWriter;
import java.util.List;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.core.KonigException;
import io.konig.core.io.ShapeFileFactory;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeVisitor;

public class GoogleAnalyticsUdfGenerator implements ShapeVisitor {
	

	private ShapeFileFactory fileFactory;
	private ShapeManager shapeManager;
	
	public GoogleAnalyticsUdfGenerator(ShapeFileFactory fileFactory,ShapeManager shapeManager) {
		this.fileFactory = fileFactory;
		this.shapeManager = shapeManager;
	}
	public GoogleAnalyticsUdfGenerator(ShapeFileFactory fileFactory) {
		this.fileFactory = fileFactory;
	}

	@Override
	public void visit(Shape shape) {
		// Get a description of the target BigQuery table (i.e. the destination table)
		GoogleBigQueryTable targetTable = getTargetTable(shape);		
		if (targetTable != null) {	
			BigQueryTableGenerator tableGenerator = new BigQueryTableGenerator(shapeManager);
			TableSchema schema = tableGenerator.toTableSchema(shape);
			String function = createQuery(schema)
					.replace("@destinationTable", targetTable.getTableIdentifier())
					.replace("@gaDataset", "131116259");
			File file = fileFactory.createFile(shape);
			writeResources(file, function);			
		}
		
	}
	
	private void createTempFunction(StringBuilder builder, List<TableFieldSchema> fieldsSchema) {
		builder.append("CREATE TEMP FUNCTION parseJson(json_str STRING) \n");
		builder.append("RETURNS STRUCT< \n");
		writeOutputSchema(builder, fieldsSchema);
		builder.append("> \n");
		builder.append("LANGUAGE js AS \"\"\" \n");
		builder.append("var row = JSON.parse(json_str); \n");
		builder.append("row['eventTime'] = new Date(row.eventTime); \n");
		builder.append("return row; \n");
		builder.append(" \"\"\"; \n");
	}
	
	private void doParsing(StringBuilder builder) {		
		builder.append("Select parseJson(h.eventInfo.eventLabel) as output \n");
		builder.append("from `@gaDataset.ga_sessions_*`,UNNEST(hits) as h \n");
		builder.append("where (_TABLE_SUFFIX > @fromDate "
				+ "AND _TABLE_SUFFIX < @toDate) "
				+ "AND h.eventInfo.eventCategory = @eventCategory \n");
		builder.append(" ) \n");	
	}
	
	private String createQuery(TableSchema outputTable) {
		StringBuilder builder = new StringBuilder();		
		createTempFunction(builder,outputTable.getFields());		
		appendInsert(builder,outputTable.getFields());		
		appendSelect(builder,outputTable.getFields());		
		doParsing(builder);
		return builder.toString();
	}
	private void appendSelect(StringBuilder builder, List<TableFieldSchema> fieldsSchema) {
		builder.append("Select ");
		for(int i = 0; i< fieldsSchema.size(); i++) { 
			builder.append("output." + fieldsSchema.get(i).getName());
			if(i!=(fieldsSchema.size()-1)) {
				builder.append(",");
			}
		}
		builder.append(" from( \n");
	}
	
	private void appendInsert(StringBuilder builder, List<TableFieldSchema> fieldsSchema) {
		builder.append("insert into @destinationTable(");
		for(int i = 0; i< fieldsSchema.size(); i++) { 
			builder.append(fieldsSchema.get(i).getName());
			if(i!=(fieldsSchema.size()-1)) {
				builder.append(",");
			}
		}
		builder.append(" ) \n");
	}
	
	public void writeOutputSchema(StringBuilder builder, List<TableFieldSchema> fieldsSchema) {		
		for(int i = 0; i< fieldsSchema.size(); i++) {   
			TableFieldSchema fieldSchema = fieldsSchema.get(i);
			writeField(builder, fieldSchema);			
			if(i!=(fieldsSchema.size()-1)) {
				builder.append(",");
			}
		}
		builder.append("\n");
	}
	public void writeField(StringBuilder builder, TableFieldSchema fieldSchema) {		
		if (fieldSchema.getMode().equals("REPEATED") && fieldSchema.getType().equals("RECORD")) {
			builder.append(fieldSchema.getName()+ " " + "ARRAY<STRUCT<");
			writeOutputSchema(builder, fieldSchema.getFields());	
			builder.append(">>");
		}
		if(fieldSchema.getMode().equals("REPEATED") && !fieldSchema.getType().equals("RECORD") ) {
			builder.append(fieldSchema.getName()+ " " + "ARRAY<");
			builder.append(fieldSchema.getType());
			builder.append(">");
		}			
		if(!fieldSchema.getMode().equals("REPEATED") && fieldSchema.getType().equals("RECORD") ) {
			builder.append(fieldSchema.getName()+ " " + "STRUCT<");
			writeOutputSchema(builder, fieldSchema.getFields());				
			builder.append(">");
		}
		
		if(!fieldSchema.getMode().equals("REPEATED") && !fieldSchema.getType().equals("RECORD") ) {
			builder.append(fieldSchema.getName() +" "+ fieldSchema.getType());	
		}			
		
	}
	
	
	private void writeResources(File file, String content) {
		try (FileWriter out = new FileWriter(file)) {
			out.write(content);
		} catch (Throwable e) {
			throw new KonigException(e);
		}
	}
	
	public void generate(List<Shape> shapeList) {
		for (Shape shape : shapeList) {			
			this.visit(shape);		
		}
	}
	
	public GoogleBigQueryTable getTargetTable(Shape shape) {

		GoogleBigQueryTable table = null;
		DataSource googleAnalytics = null;
		List<DataSource> list = shape.getShapeDataSource();
	
		if (list != null) {
			for (DataSource ds : list) {
				if (Konig.GoogleAnalytics.equals(ds.getId())) {
					googleAnalytics = ds;
				}
				if (ds instanceof GoogleBigQueryTable) {
					table = (GoogleBigQueryTable) ds;
				}
			}
		}
		return googleAnalytics==null ? null : table;
	}
}

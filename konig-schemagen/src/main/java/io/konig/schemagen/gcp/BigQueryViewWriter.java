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
import java.io.IOException;

import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableReference;

import io.konig.core.KonigException;
import io.konig.core.util.IOUtil;

public class BigQueryViewWriter implements BigQueryTableVisitor {
	
	private File baseDir;

	public BigQueryViewWriter(File baseDir) {
		this.baseDir = baseDir;
		baseDir.mkdirs();
	}

	@Override
	public void visit(Table table) {
		
		File tableFile = tableFile(table);
		JacksonFactory factory = JacksonFactory.getDefaultInstance();

		FileWriter writer = null;
		try {
			writer = new FileWriter(tableFile); 
			JsonGenerator generator = factory.createJsonGenerator(writer);
			generator.enablePrettyPrint();
			generator.serialize(table);
			generator.flush();
		
		} catch (IOException e) {
			throw new KonigException(e);
		} finally {
			IOUtil.close(writer, tableFile.getName());
		}
		
	}

	private File tableFile(Table table) {
		
		TableReference tableRef = table.getTableReference();
		if (tableRef == null) {
			throw new KonigException("Table reference");
		}
		String tableId = tableRef.getTableId();
		if (tableId == null) {
			throw new KonigException("tableId is not defined");
		}
		String datasetId = tableRef.getDatasetId();
		if (datasetId == null) {
			throw new KonigException("Dataset Id is not defined for table: " + tableId);
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(datasetId);
		builder.append('.');
		builder.append(tableId);
		builder.append(".json");

		return new File(baseDir, builder.toString());
	}
	


}

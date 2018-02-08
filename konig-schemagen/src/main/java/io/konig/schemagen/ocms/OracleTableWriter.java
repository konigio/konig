package io.konig.schemagen.ocms;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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
import java.text.MessageFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.core.KonigException;
import io.konig.omcs.datasource.OracleTable;
import io.konig.omcs.datasource.OracleTableDefinition;
import io.konig.schemagen.sql.CreateOracleTableStatement;
import io.konig.schemagen.sql.SqlTable;
import io.konig.schemagen.sql.SqlTableGenerator;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class OracleTableWriter implements ShapeVisitor {
	
	private File baseDir;
	private SqlTableGenerator generator;
	
	public OracleTableWriter(File baseDir, SqlTableGenerator generator) {
		this.baseDir = baseDir;
		this.generator = generator;
	}
	
	@Override
	public void visit(Shape shape) {
		OracleTable table = shape.findDataSource(OracleTable.class);
		if (table != null) {
			OracleTableDefinition tableDefinition = new OracleTableDefinition();
			SqlTable sqlTable = generator.generateTable(shape);
			File sqlFile = sqlFile(table);
			File jsonFile = jsonFile(table);
			tableDefinition.setQuery(new CreateOracleTableStatement(sqlTable).toString());
			tableDefinition.setTableReference(table.getTableReference());
			writeTable(sqlFile, jsonFile, tableDefinition);
		}
	}
	

	private void writeTable(File sqlFile, File jsonFile, OracleTableDefinition table) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			
			String ddl = table.getQuery();
			table.setQuery(sqlFile.getName());
			
			
			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}
			writeDDL(sqlFile, ddl);
			mapper.writeValue(jsonFile, table);
		} catch (IOException e) {
			throw new KonigException(e);
		}
	}
	
	private void writeDDL(File sqlFile, String query) throws IOException {
		
		try (FileWriter fileWriter = new FileWriter(sqlFile)) {
			fileWriter.write(query);
		}
		
	}

	private File file(OracleTable table, String suffix) {

		String instanceId = table.getTableReference().getOmcsInstanceId();
		String schema = table.getTableReference().getOracleSchema();
		String tableName = table.getTableName();
		
		String fileName = MessageFormat.format("{0}_{1}_{2}.{3}", instanceId, schema, tableName, suffix);
		
		File file = new File(baseDir, fileName);
		return file;
	}
	private File jsonFile(OracleTable table) {
		return file(table, "json");
	}

	private File sqlFile(OracleTable table) {
		return file(table, "sql");
	}
}

package io.konig.schemagen.aws;

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

import io.konig.abbrev.AbbreviationManager;
import io.konig.aws.datasource.AwsAuroraDefinition;
import io.konig.aws.datasource.AwsAuroraTable;
import io.konig.aws.datasource.AwsAuroraTableReference;
import io.konig.aws.datasource.AwsAuroraView;
import io.konig.core.KonigException;
import io.konig.core.project.ProjectFile;
import io.konig.core.project.ProjectFolder;
import io.konig.schemagen.sql.SqlTable;
import io.konig.schemagen.sql.SqlTableGenerator;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class AwsAuroraTableWriter implements ShapeVisitor {
	
	private File baseDir;
	private SqlTableGenerator generator;
	private AbbreviationManager abbrevManager;
	private ProjectFolder folder;
	
	public AwsAuroraTableWriter(File baseDir,SqlTableGenerator generator, ProjectFolder folder, AbbreviationManager abbrevManager) {
		this.baseDir = baseDir;
		this.generator = generator;
		this.abbrevManager=abbrevManager;
	}

	@Override
	public void visit(Shape shape) {
		AwsAuroraTable table = shape.findDataSource(AwsAuroraTable.class);
		AwsAuroraView view =shape.findDataSource(AwsAuroraView.class);
		if (table != null && view==null) {
			AwsAuroraDefinition tableDefinition = new AwsAuroraDefinition();
			AwsAuroraTableReference tableReference = table.getTableReference();
			SqlTable sqlTable = generator.generateTable(shape,abbrevManager);
			File file = sqlFile(table);
			String tableName = sqlTable.getTableName();
			String schemaName = tableReference.getAwsSchema();
			if (schemaName != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(schemaName);
				builder.append('.');
				builder.append(tableName);
				sqlTable.setTableName(builder.toString());
			}
			writeDDL(file, sqlTable);
			sqlTable.setTableName(tableName);
			tableDefinition.setQuery(file.getName());
			tableDefinition.setTableReference(table.getTableReference());
			File jsonFile = jsonFile(tableReference);
			writeTable(jsonFile, tableDefinition);
		}

	}
	private void writeTable(File jsonFile, AwsAuroraDefinition table) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(jsonFile, table);
		} catch (IOException e) {
			throw new KonigException(e);
		}
	}
	
	private void writeDDL(File file, SqlTable sqlTable) {
		if (!baseDir.exists()) {
			baseDir.mkdirs();
		}
		try (FileWriter out = new FileWriter(file)) {
			String text = sqlTable.toString();
			out.write("CREATE TABLE IF NOT EXISTS ");
			out.write(text);
			
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
	}

	private File fileName(AwsAuroraTableReference table, String fileType) {
		String instance = table.getAwsAuroraHost();
		String database = table.getAwsSchema();
		String tableName = table.getAwsTableName();
		
		String fileName = MessageFormat.format("{0}_{1}_{2}.{3}", instance, database, tableName , fileType);
		
		return new File(baseDir, fileName);
	}
	
	private File jsonFile(AwsAuroraTableReference table) {
		return fileName(table, "json");
	}

	private File sqlFile(AwsAuroraTable table) {
		ProjectFile file = folder.createFile(table.getDdlFileName());
		table.setDdlFile(file);
		
		return file.getLocalFile();
	}
}

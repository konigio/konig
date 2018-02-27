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

import io.konig.aws.datasource.AwsAurora;
import io.konig.core.KonigException;
import io.konig.schemagen.sql.SqlTable;
import io.konig.schemagen.sql.SqlTableGenerator;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class AwsAuroraTableWriter implements ShapeVisitor {
	
	private File baseDir;
	private SqlTableGenerator generator;


	
	public AwsAuroraTableWriter(File baseDir, SqlTableGenerator generator) {
		this.baseDir = baseDir;
		this.generator = generator;
	}

	@Override
	public void visit(Shape shape) {
		AwsAurora table = shape.findDataSource(AwsAurora.class);
		if (table != null) {
			SqlTable sqlTable = generator.generateTable(shape);
			File file = sqlFile(table);
			writeTable(file, sqlTable);
		}

	}

	private void writeTable(File file, SqlTable sqlTable) {
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

	private File sqlFile(AwsAurora table) {
		String instance = table.getAwsAuroraHost();
		String database = table.getAwsSchema();
		String tableName = table.getAwsTableName();
		
		String fileName = MessageFormat.format("{0}_{1}_{2}.sql", instance, database, tableName);
		
		return new File(baseDir, fileName);
	}

}

package io.konig.schemagen.gcp;

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

import io.konig.core.KonigException;
import io.konig.datasource.DatasourceFileLocator;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.schemagen.sql.SqlTable;
import io.konig.schemagen.sql.SqlTableGenerator;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class CloudSqlTableWriter implements ShapeVisitor {
	private File baseDir;
	private SqlTableGenerator generator;
	private DatasourceFileLocator sqlFileLocator;

	// TODO: Add a DatasourceFileLocator as a private field, and pass an instance to the constructor.
	// TODO: Remove the baseDir field.
	
	public CloudSqlTableWriter(SqlTableGenerator generator, DatasourceFileLocator sqlFileLocator) {
		//this.baseDir = baseDir;
		this.generator = generator;
		this.sqlFileLocator= sqlFileLocator;
	}

	@Override
	public void visit(Shape shape) {
		
		GoogleCloudSqlTable table = shape.findDataSource(GoogleCloudSqlTable.class);
		if (table != null) {
			SqlTable sqlTable = generator.generateTable(shape);
			File file = sqlFile(table);
			
			writeTable(file, sqlTable);
		}

	}

	private void writeTable(File file, SqlTable sqlTable) {

	/*	if (!baseDir.exists()) {
			baseDir.mkdirs();
		}*/
		try (FileWriter out = new FileWriter(file)) {
			
			
			String text = sqlTable.toString();
			out.write("CREATE TABLE IF NOT EXISTS ");
			out.write(text);
			
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
	}

	private File sqlFile(GoogleCloudSqlTable table) {
		// TODO: Refactor this method to use the DatasourceFileLocator passed to the constructor.
		/*String instance = table.getInstance();
		String database = table.getDatabase();
		String tableName = table.getTableName();
		
		String fileName = MessageFormat.format("{0}_{1}_{2}.sql", instance, database, tableName);*/
		
		return sqlFileLocator.locateFile(table);
	}

	

}

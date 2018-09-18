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

import io.konig.abbrev.AbbreviationManager;
import io.konig.core.KonigException;
import io.konig.core.project.ProjectFile;
import io.konig.core.project.ProjectFolder;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.schemagen.sql.SqlTable;
import io.konig.schemagen.sql.SqlTableGenerator;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class CloudSqlTableWriter implements ShapeVisitor {
	private SqlTableGenerator generator;
	private ProjectFolder folder;
	private AbbreviationManager abbrevManager;

	
	public CloudSqlTableWriter(SqlTableGenerator generator, ProjectFolder folder, AbbreviationManager abbrevManager) {
		this.generator = generator;
		this.folder = folder;
		this.abbrevManager=abbrevManager;
	}

	@Override
	public void visit(Shape shape) {
		
		GoogleCloudSqlTable table = shape.findDataSource(GoogleCloudSqlTable.class);
		if (table != null) {
			SqlTable sqlTable = generator.generateTable(shape,abbrevManager);
			File file = sqlFile(table);
			
			writeTable(file, sqlTable);
		}

	}

	private void writeTable(File file, SqlTable sqlTable) {

		if (!folder.exists()) {
			folder.mkdirs();
		}
		try (FileWriter out = new FileWriter(file)) {
			
			
			String text = sqlTable.toString();
			out.write("CREATE TABLE IF NOT EXISTS ");
			out.write(text);
			
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
	}

	private File sqlFile(GoogleCloudSqlTable table) {
		ProjectFile file = folder.createFile(table.getDdlFileName());
		table.setDdlFile(file);
		
		return file.getLocalFile();
	}

	

}

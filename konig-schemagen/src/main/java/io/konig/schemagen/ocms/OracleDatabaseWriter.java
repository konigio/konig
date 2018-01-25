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
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.core.KonigException;
import io.konig.omcs.datasource.OracleDatabaseRefence;
import io.konig.omcs.datasource.OracleTable;
import io.konig.omcs.datasource.OracleTableDefinition;
import io.konig.schemagen.sql.CreateDatabaseStatement;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class OracleDatabaseWriter implements ShapeVisitor {

	private File baseDir;
	
	public OracleDatabaseWriter(File baseDir) {
		this.baseDir = baseDir;
	}
	
	@Override
	public void visit(Shape shape) {
		OracleTable table = shape.findDataSource(OracleTable.class);
		if (table != null) {
			String instanceId = table.getTableReference().getOmcsInstanceId();
			String databaseId = table.getTableReference().getOmcsDatabaseId();
			OracleTableDefinition tableDefinition = new OracleTableDefinition();
			OracleDatabaseRefence databaseReference = new OracleDatabaseRefence(instanceId, databaseId);
			File file = sqlFile(table);
			CreateDatabaseStatement statement = new CreateDatabaseStatement(instanceId);
			tableDefinition.setQuery(statement.toString());
			tableDefinition.setDatabaseReference(databaseReference);
			writeDatabase(file, tableDefinition);
		}
	}

	private void writeDatabase(File file, OracleTableDefinition table) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (!baseDir.exists()) {
				baseDir.mkdirs();
			}
			if(!file.exists()) {
				mapper.writeValue(file, table);
			}
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
	}

	private File sqlFile(OracleTable table) {
		String fileName = table.getTableReference().getOmcsDatabaseId() + ".json";
		return new File(baseDir, fileName);
	}

}

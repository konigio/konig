package io.konig.schemagen.sql;

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


import java.util.List;

import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.omcs.datasource.OracleTable;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.Shape;

public class SqlTableNameFactory {

	
	public String getTableName(Shape shape) throws SchemaGeneratorException {
		
		List<DataSource> list = shape.getShapeDataSource();
		if (list != null) {
			for (DataSource ds : list) {
				if (ds instanceof GoogleCloudSqlTable) {
					GoogleCloudSqlTable table = (GoogleCloudSqlTable) ds;
					return table.getTableName();
				}
				if (ds instanceof OracleTable) {
					OracleTable table = (OracleTable) ds;
					return table.getTableName();
				}
			}
		}
		
		throw new SchemaGeneratorException("Table name not found for Shape: " + shape.getId());
	}

}

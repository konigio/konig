package org.konig.omcs.datasource;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

/*
 * #%L
 * Konig Oracle Managed Cloud Model
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


import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;

public class OracleTable extends DataSource implements TableDataSource {
	private String tableName;
	private String instanceId;
	private String databaseId;
	private String omcstablesId;
	
	public String getTableName() {
		return omcstablesId;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableIdentifier() {
		return tableName;
	}
	
	public OracleTableReference getTableReference() throws Exception {
		
		if (instanceId == null) {
			throw new Exception("instanceId is not defined");
		}
		if (databaseId == null) {
			throw new Exception("databaseId is not defined for instance " + instanceId);
		}
		if (omcstablesId == null) {
			throw new Exception("omcstablesId is not defined for database " + databaseId);
		}
		
		
		return new OracleTableReference(instanceId, databaseId, omcstablesId);
	}

	@Override
	public void setId(Resource id) {
		super.setId(id);
		if (tableName == null && id instanceof URI) {
			URI uri = (URI) id;
			tableName = uri.getLocalName();
		}
	}
}

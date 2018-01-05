package io.konig.gcp.datasource;

/*
 * #%L
 * Konig Google Cloud Platform Model
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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;

public class GoogleCloudSqlTable  extends DataSource implements TableDataSource {
	private String tableName;

	public GoogleCloudSqlTable() {
	}

	@Override
	public String getTableIdentifier() {
		return tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
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
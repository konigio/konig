package io.konig.omcs.datasource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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

public class OracleTableDefinition {

	@JsonInclude(Include.NON_NULL)
	private OracleTableReference tableReference;
	@JsonInclude(Include.NON_NULL)
	private OracleDatabaseRefence databaseReference;

	private String query;

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	public OracleTableReference getTableReference() {
		return tableReference;
	}

	public void setTableReference(OracleTableReference tableReference) {
		this.tableReference = tableReference;
	}

	public OracleDatabaseRefence getDatabaseReference() {
		return databaseReference;
	}

	public void setDatabaseReference(OracleDatabaseRefence databaseReference) {
		this.databaseReference = databaseReference;
	}
}

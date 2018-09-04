package io.konig.omcs.datasource;

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

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.KonigException;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OMCS;
import io.konig.datasource.TableDataSource;

public class OracleTable extends TableDataSource {
	private String tableName;
	private OracleTableReference tableReference;
	private String tableId;
	
	public OracleTable() {
		addType(Konig.OracleTable);
	}
	
	public void setTableId(String tableId){
		this.tableId = tableId;
	}
	
	public String getTableId(){
		return tableId;
	}
	
	@RdfProperty(OMCS.TABLE_REFERENCE)
	public OracleTableReference getTableReference() {
		return tableReference;
	}
	
	public void setTableReference(OracleTableReference tableReference) {
		this.tableReference = tableReference;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableIdentifier() {
		return tableName;
	}

	@Override
	public void setId(Resource id) {
		super.setId(id);
		if (tableName == null && id instanceof URI) {
			URI uri = (URI) id;
			tableName = uri.getLocalName();
		}
	}

	@Override
	public String getUniqueIdentifier() {
		StringBuilder builder = new StringBuilder();
		builder.append("OracleTable:");
		builder.append(tableReference.getOmcsInstanceId());
		builder.append(':');
		builder.append(tableReference.getOracleSchema());
		builder.append(':');
		builder.append(tableReference.getOmcsTableId());
		return builder.toString();
	}

	@Override
	public String getSqlDialect() {
		// TODO: Need to supply the version number supported.
		return "PL/SQL";
	}

	@Override
	public String getDdlFileName() {
		OracleTableReference ref = getTableReference();
		StringBuilder builder = new StringBuilder();
		builder.append(ref.getOmcsInstanceId());
		builder.append('.');
		builder.append(ref.getOracleSchema());
		builder.append('.');
		builder.append(ref.getOmcsTableId());
		builder.append(".sql");
		return builder.toString();
	}

	@Override
	public String getQualifiedTableName() {
		if (tableReference == null) {
			throw new KonigException("tableReference must be defined");
		}
		StringBuilder builder = new StringBuilder();
		builder.append(tableReference.getOracleSchema());
		builder.append('.');
		builder.append(tableName);
		return builder.toString();
	}

}

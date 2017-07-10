package io.konig.gcp.datasource;

/*
 * #%L
 * Konig Core
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


import io.konig.annotation.RdfProperty;
import io.konig.core.KonigException;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;

public class GoogleSpannerTable extends DataSource implements TableDataSource {
	
	private SpannerTableReference tableReference;

	public GoogleSpannerTable() {
		addType(Konig.GoogleSpannerTable);
	}

	@RdfProperty(GCP.TABLE_REFERENCE)
	public SpannerTableReference getTableReference() {
		return tableReference;
	}

	public void setTableReference(SpannerTableReference tableReference) {
		this.tableReference = tableReference;
	}

	@Override
	public String getTableIdentifier() {
		
		if (tableReference == null) {
			throw new KonigException("tableReference must be defined for <" + getId() + ">");
		}
		
		StringBuilder builder = new StringBuilder();
		String databaseId = tableReference.getDatabaseName();
		String tableName = tableReference.getTableName();
		
		builder.append(databaseId);
		builder.append('.');
		builder.append(tableName);
		return builder.toString();
	}
	
}

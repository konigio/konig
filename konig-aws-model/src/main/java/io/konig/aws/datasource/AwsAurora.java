package io.konig.aws.datasource;

/*
 * #%L
 * Konig AWS Model
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
import io.konig.core.vocab.AWS;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;

public class AwsAurora extends DataSource implements TableDataSource{
	private AwsAuroraTableReference tableReference;
	private String awsTableName;
	private String rdbmsFieldNamespace;
	
	public AwsAurora() {
	}

	public String getAwsTableName() {
		return awsTableName;
	}

	public void setAwsTableName(String awsTableName) {
		this.awsTableName = awsTableName;
	}

	@RdfProperty(AWS.TABLE_REFERENCE)
	public AwsAuroraTableReference getTableReference() {
		return tableReference;
	}
	
	public void setTableReference(AwsAuroraTableReference tableReference) {
		this.tableReference = tableReference;
	}

	@Override
	public void setId(Resource id) {
		super.setId(id);
		if (awsTableName == null && id instanceof URI) {
			URI uri = (URI) id;
			awsTableName = uri.getLocalName();
		}
	}


	@Override
	public String getTableIdentifier() {
		return awsTableName;
	}

	@Override
	public String getUniqueIdentifier() {
		StringBuilder builder = new StringBuilder();
		builder.append("AwsAurora:");
		builder.append(tableReference.getAwsSchema());
		builder.append(':');
		builder.append(tableReference.getAwsTableName());
		return builder.toString();
	}

	@Override
	public String getSqlDialect() {
		return "MySQL 5.7";
	}

	@RdfProperty(Konig.RDBMS_FIELD_NAMESPACE)
	public String getRdbmsFieldNamespace() {
		return rdbmsFieldNamespace;
	}

	public void setRdbmsFieldNamespace(String rdbmsFieldNamespace) {
		this.rdbmsFieldNamespace = rdbmsFieldNamespace;
	}
}

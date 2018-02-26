package io.konig.aws.datasource;

/*
 * #%L
 * Konig AWS
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

import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;

public class AwsAurora extends DataSource implements TableDataSource {
	private String awsAuroraHost;
	private String awsSchema;
	private String awsTableName;

	public AwsAurora() {
	}

	
	public String getAwsAuroraHost() {
		return awsAuroraHost;
	}


	public String getAwsSchema() {
		return awsSchema;
	}


	public String getAwsTableName() {
		return awsTableName;
	}


	public void setAwsAuroraHost(String awsAuroraHost) {
		this.awsAuroraHost = awsAuroraHost;
	}


	public void setAwsSchema(String awsSchema) {
		this.awsSchema = awsSchema;
	}


	public void setAwsTableName(String awsTableName) {
		this.awsTableName = awsTableName;
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
}

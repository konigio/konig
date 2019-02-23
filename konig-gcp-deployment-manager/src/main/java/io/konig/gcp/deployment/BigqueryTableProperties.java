package io.konig.gcp.deployment;

import com.google.api.services.bigquery.model.ExternalDataConfiguration;

/*
 * #%L
 * Konig GCP Deployment Manager
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


import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;

/**
 * A POJO for the bigquery.v2.table resource.
 * Ideally, this class would extend com.google.api.services.bigquery.model.Table by
 * adding the "datasetId" field.  But that Table class is declared to be final.
 * So we must essentially replicate it.
 * 
 * @author Greg McFall
 *
 */
public class BigqueryTableProperties  {

	private String datasetId;
	private TableSchema schema;
	private TableReference tableReference;
	private ExternalDataConfiguration externalDataConfiguration;
	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
	public TableSchema getSchema() {
		return schema;
	}
	public void setSchema(TableSchema schema) {
		this.schema = schema;
	}
	public TableReference getTableReference() {
		return tableReference;
	}
	public void setTableReference(TableReference tableReference) {
		this.tableReference = tableReference;
	}
	public ExternalDataConfiguration getExternalDataConfiguration() {
		return externalDataConfiguration;
	}
	public void setExternalDataConfiguration(ExternalDataConfiguration externalDataConfiguration) {
		this.externalDataConfiguration = externalDataConfiguration;
	}
	
	
	
}

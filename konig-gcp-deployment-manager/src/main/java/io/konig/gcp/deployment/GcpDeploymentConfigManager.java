package io.konig.gcp.deployment;

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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableReference;

import io.konig.gcp.common.BigQueryTableListener;
import io.konig.yaml.AnchorFeature;
import io.konig.yaml.YamlWriter;

public class GcpDeploymentConfigManager {

	private DeploymentConfig config = new DeploymentConfig();
	private Set<String> bigqueryDatasets = new HashSet<>();
	
	
	public BigQueryTableListener createBigQueryTableListener() {
		return new MyBigQueryTableListener();
	}
	
	
	public DeploymentConfig getConfig() {
		return config;
	}
	
	public void writeConfig(File configFile) throws IOException {
		if (!config.getResources().isEmpty()) {
			configFile.getParentFile().mkdirs();
			try (YamlWriter yaml = new YamlWriter(new FileWriter(configFile))) {
				yaml.setAnchorFeature(AnchorFeature.NONE);
				yaml.setIncludeClassTag(false);
				yaml.write(config);
			}
		}
	}

	private class MyBigQueryTableListener implements BigQueryTableListener {

		@Override
		public void handleTable(Table table) {
			
			String resourceName = bigQueryTableName(
					table.getTableReference().getDatasetId(), 
					table.getTableReference().getTableId());
			
			BigqueryTableResource resource = new BigqueryTableResource();
			resource.setName(resourceName);
			BigqueryTableProperties properties = new BigqueryTableProperties();
			resource.setProperties(properties);
			properties.setDatasetId(table.getTableReference().getDatasetId());
			properties.setTableReference(copy(table.getTableReference()));
			properties.setSchema(table.getSchema());
			GcpMetadata metadata = new GcpMetadata();
			resource.setMetadata(metadata);
			
			String datasetResourceName = bigQueryDatasetName(table.getTableReference().getDatasetId());
			metadata.addDependency(datasetResourceName);
			
			if (!bigqueryDatasets.contains(datasetResourceName)) {
				bigqueryDatasets.add(datasetResourceName);
				BigqueryDatasetResource datasetResource = new BigqueryDatasetResource();
				datasetResource.setName(datasetResourceName);
				BigqueryDatasetProperties datasetProperties = new BigqueryDatasetProperties();
				datasetResource.setProperties(datasetProperties);
				BigqueryDatasetReference datasetReference = new BigqueryDatasetReference();
				datasetReference.setDatasetId(table.getTableReference().getDatasetId());
				datasetProperties.setDatasetReference(datasetReference);
				
				config.addResource(datasetResource);
			}
			
			config.addResource(resource);
		}
		
		private TableReference copy(TableReference tableReference) {
			TableReference clone = new TableReference();
			clone.setDatasetId(tableReference.getDatasetId());
			clone.setTableId(tableReference.getTableId());
			return clone;
		}

		private String bigQueryDatasetName(String datasetId) {
			return "bq-dataset-" + datasetId;
		}
		
		private String bigQueryTableName(String datasetId, String tableId) {
			return "bq-table-" + datasetId + "-" + tableId;
		}
		
	}

}

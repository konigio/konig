package io.konig.gcp.deployment;

import java.util.List;

/*
 * #%L
 * Konig GCP Deployment Manager
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import org.openrdf.model.URI;

import com.google.api.services.bigquery.model.ExternalDataConfiguration;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.datasource.DataSource;
import io.konig.datasource.DataSourceVisitor;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.schemagen.gcp.BigQueryTableGenerator;
import io.konig.shacl.Shape;

public class GoogleBigqueryTableVisitor implements DataSourceVisitor {
	
	private GcpConfigManager manager;
	private BigQueryTableGenerator tableGenerator;

	public GoogleBigqueryTableVisitor(GcpConfigManager manager, BigQueryTableGenerator tableGenerator) {
		this.manager = manager;
		this.tableGenerator = tableGenerator;
	}

	@Override
	public void visit(Graph graph, Shape shape, DataSource ds) {
		
		if (ds instanceof GoogleBigQueryTable) {
			GoogleBigQueryTable bigquery = (GoogleBigQueryTable) ds;
			handle(graph, shape, bigquery);
		} else if (ds instanceof GoogleCloudStorageBucket) {
			handleStorageBucket(graph, (GoogleCloudStorageBucket) ds);
		}
	}

	private void handleStorageBucket(Graph graph, GoogleCloudStorageBucket bucket) {
		String bucketName = bucket.getName();
		String configName = manager.storageBucketConfigName(bucketName);
		
		StorageBucketResource resource = manager.getConfig().findResource(bucketName, StorageBucketResource.class);
		if (resource == null) {
			resource = new StorageBucketResource();
			resource.setName(bucketName);
			StorageBucketProperties properties = new StorageBucketProperties();
			resource.setProperties(properties);
			properties.setName(bucketName);
			manager.getConfig().addResource(resource);
			// TODO: add other properties
		}
		
		
	}

	private void handle(Graph graph, Shape shape, GoogleBigQueryTable bigquery) {
		
		addDataset(graph, bigquery);
		addTable(graph, shape, bigquery);
		
		
		
		
	}

	private void addStorageBucket(GoogleBigQueryTable bigquery, BigqueryTableResource table) {
		
		ExternalDataConfiguration ext = bigquery.getExternalDataConfiguration();
		if (ext != null) {
			List<String> list = ext.getSourceUris();
			if (list != null) {
				for (String uri : list) {
					if (uri.startsWith("gs://")) {
						String bucketName = manager.storageBucketName(uri);
						String configName = manager.storageBucketConfigName(bucketName);
						
						StorageBucketResource resource = manager.getConfig().findResource(bucketName, StorageBucketResource.class);
						if (resource == null) {
							resource = new StorageBucketResource();
							resource.setName(bucketName);
							StorageBucketProperties properties = new StorageBucketProperties();
							properties.setName(bucketName);
							resource.setProperties(properties);
							
							manager.getConfig().addResource(resource);
							
							table.produceMetadata().addDependency(bucketName);
						}
					}
				}
			}
		}
		
	}

	private void addTable(Graph graph, Shape shape, GoogleBigQueryTable bigquery) {
		
		String datasetId = bigquery.getTableReference().getDatasetId();
		String tableId = bigquery.getTableReference().getTableId();
		
		String resourceName = manager.bigqueryTableName(datasetId, tableId);

		DeploymentConfig config = manager.getConfig();
		BigqueryTableResource resource = config.findResource(resourceName, BigqueryTableResource.class);
		if (resource == null) {
			resource = new BigqueryTableResource();
			resource.setName(resourceName);
			config.addResource(resource);
			BigqueryTableProperties properties = null;
			
			URI resourceIri = manager.bigqueryTableIri(datasetId, tableId);
			
			
			Vertex v = graph.getVertex(resourceIri);
			if (v != null) {
				SimplePojoFactory factory = new SimplePojoFactory();
				properties = factory.create(v, BigqueryTableProperties.class);
			} else {
				properties = new BigqueryTableProperties();
			}

			resource.setProperties(properties);
			properties.setExternalDataConfiguration(bigquery.getExternalDataConfiguration());
			
			properties.setDatasetId(datasetId);
			TableReference tableReference = new TableReference();
			properties.setTableReference(tableReference);
			tableReference.setDatasetId(datasetId);
			tableReference.setTableId(tableId);
			
			TableSchema tableSchema = tableGenerator.toTableSchema(shape);
			properties.setSchema(tableSchema);
			
			addStorageBucket(bigquery, resource);
		}
		
		
	}

	private void addDataset(Graph graph, GoogleBigQueryTable bigquery) {
		String datasetId = bigquery.getTableReference().getDatasetId();
		URI datasetIri = manager.datasetIri(datasetId);
		
		DeploymentConfig config = manager.getConfig();
		String datasetName = manager.datasetName(datasetId);
		BigqueryDatasetResource resource = config.findResource(datasetName, BigqueryDatasetResource.class);
		if (resource == null) {
			resource = new BigqueryDatasetResource();
			resource.setName(datasetName);
			config.addResource(resource);
			BigqueryDatasetProperties properties = null;

			Vertex v = graph.getVertex(datasetIri);
			if (v != null) {
				SimplePojoFactory factory = new SimplePojoFactory();
				properties = factory.create(v, BigqueryDatasetProperties.class);
			} else {
				properties = new BigqueryDatasetProperties();
			}
			
			resource.setProperties(properties);
			BigqueryDatasetReference reference = new BigqueryDatasetReference();
			reference.setDatasetId(datasetId);
			properties.setDatasetReference(reference);

		}
		
	}

}

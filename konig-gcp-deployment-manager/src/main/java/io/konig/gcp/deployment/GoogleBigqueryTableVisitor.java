package io.konig.gcp.deployment;

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

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.GCP;
import io.konig.datasource.DataSource;
import io.konig.datasource.DataSourceVisitor;
import io.konig.gcp.datasource.GoogleBigQueryTable;

public class GoogleBigqueryTableVisitor implements DataSourceVisitor {
	
	private GcpConfigManager manager;

	public GoogleBigqueryTableVisitor(GcpConfigManager manager) {
		this.manager = manager;
	}

	@Override
	public void visit(Graph graph, DataSource ds) {
		
		if (ds instanceof GoogleBigQueryTable) {
			GoogleBigQueryTable bigquery = (GoogleBigQueryTable) ds;
			handle(graph, bigquery);
		}
	}

	private void handle(Graph graph, GoogleBigQueryTable bigquery) {
		
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

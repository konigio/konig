package io.konig.maven;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
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


import java.io.File;
import java.io.IOException;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;

import io.konig.gcp.common.GoogleCloudService;

public class CreateDatasetAction {

	private KonigDeployment deployment;
	
	public CreateDatasetAction(KonigDeployment deployment) {
		this.deployment = deployment;
	}
	
	public KonigDeployment from(String datasetFile) throws IOException {
		GoogleCloudService service = deployment.getService();
		File file = deployment.file(datasetFile);
		BigQuery bigquery = service.bigQuery();
		try {
			DatasetInfo info = service.readDatasetInfo(file);
			Dataset dataset = bigquery.getDataset(info.getDatasetId());
			if (dataset == null) {
				Dataset datasetResponse = bigquery.create(info);
				deployment.setResponse("Created  Dataset " + datasetResponse.getDatasetId());
			}
			else{
				deployment.setResponse("Dataset already available ");
			}
		} catch (Exception ex) {
			throw ex;
		}
		return deployment;
	}

}

package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.vocab.Konig;

public class GoogleCloudProject {

	private String projectId;
	private List<BigQueryDataset> datasetList = new ArrayList<>();
	private List<SpannerDatabase> databaseList = new ArrayList<>();
	
	public URI getType() {
		return Konig.GoogleCloudProject;
	}
	
	public String getProjectId() {
		return projectId;
	}
	
	public GoogleCloudProject setProjectId(String projectId) {
		this.projectId = projectId;
		return this;
	}
	
	
	public List<BigQueryDataset> getProjectDataset() {
		return datasetList;
	}
	
	
	public List<SpannerDatabase> getProjectDatabase() {
		return databaseList;
	}
	
	
	public void addProjectDataset(BigQueryDataset dataset) {
		datasetList.add(dataset);
		dataset.setDatasetProject(this);
	}
	
	public void addProjectDatabase(SpannerDatabase database) {
		databaseList.add(database);
		database.setDatabaseProject(this);
	}
	
	
	public BigQueryDataset findProjectDataset(String datasetId) {
		for (BigQueryDataset s : datasetList) {
			if (datasetId.equals(s.getDatasetId())) {
				return s;
			}
		}
		return null;
	}
	
	public SpannerDatabase findProjectSpannerDatabase(String databaseId) {
		for (SpannerDatabase s : databaseList) {
			if (databaseId.equals(s.getDatabaseName())) {
				return s;
			}
		}
		return null;
	}
	
	
}

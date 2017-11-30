package io.konig.maven;

/*
 * #%L
 * Konig Google Cloud Platform Deployment Model
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

import io.konig.yaml.Yaml;

public class BigQueryInfo {
	

	@Parameter(property="konig.gcp.bigquery.directory", defaultValue="${konig.gcp.directory}/bigquery")
	private File directory;

	@Parameter(property="konig.gcp.bigquery.dataset", defaultValue="${konig.gcp.bigquery.directory}/dataset")
	private File dataset;

	@Parameter(property="konig.gcp.bigquery.schema", defaultValue="${konig.gcp.bigquery.directory}/schema")
	private File schema;
	
	@Parameter(property="konig.gcp.bigquery.view", defaultValue="${konig.gcp.bigquery.directory}/view")
	private File view;

	@Parameter(property="konig.gcp.bigquery.scripts", defaultValue="${konig.gcp.bigquery.directory}/scripts")
	private File scripts;

	@Parameter(property="konig.gcp.bigquery.data", defaultValue="${konig.gcp.bigquery.directory}/data")
	private File data;
	
	@Parameter(property="konig.gcp.bigquery.metadata", required=true)
	private MetadataInfo metadata;
	
	
	public File getDirectory() {
		return directory;
	}
	public void setDirectory(File directory) {
		this.directory = directory;
	}
	public File getDataset() {
		return dataset;
	}
	public void setDataset(File datasets) {
		this.dataset = datasets;
	}
	public File getSchema() {
		return schema;
	}
	public void setSchema(File schema) {
		this.schema = schema;
	}
	public File getView() {
		return view;
	}
	public void setView(File view) {
		this.view = view;
	}
	public File getScripts() {
		return scripts;
	}
	public void setScripts(File scripts) {
		this.scripts = scripts;
	}
	public File getData() {
		return data;
	}
	public void setData(File data) {
		this.data = data;
	}
	public String toString() {
		return Yaml.toString(this);
	}
	public MetadataInfo getMetadata() {
		return metadata;
	}
	public void setMetadata(MetadataInfo metadata) {
		this.metadata = metadata;
	}
	
	
	

}
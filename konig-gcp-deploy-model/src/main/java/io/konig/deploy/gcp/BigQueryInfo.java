package io.konig.deploy.gcp;

import java.io.File;

import io.konig.yaml.Yaml;

public class BigQueryInfo {
	

	@Parameter(property="konig.deploy.gcp.bigquery.directory", defaultValue="${konig.deploy.gcp.directory}/bigquery")
	private File directory;

	@Parameter(property="konig.deploy.gcp.bigquery.dataset", defaultValue="${konig.deploy.gcp.bigquery.directory}/dataset")
	private File dataset;

	@Parameter(property="konig.deploy.gcp.bigquery.schema", defaultValue="${konig.deploy.gcp.bigquery.directory}/schema")
	private File schema;

	@Parameter(property="konig.deploy.gcp.bigquery.scripts", defaultValue="${konig.deploy.gcp.bigquery.directory}/scripts")
	private File scripts;

	@Parameter(property="konig.deploy.gcp.bigquery.data", defaultValue="${konig.deploy.gcp.bigquery.directory}/data")
	private File data;
	
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
	
	

}
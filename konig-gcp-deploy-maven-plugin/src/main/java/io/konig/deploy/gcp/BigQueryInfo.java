package io.konig.deploy.gcp;

import java.io.File;

import io.konig.yaml.Yaml;

public class BigQueryInfo {
	private static final String BIGQUERY = "bigquery";
	private static final String DATASET = "dataset";
	private static final String SCHEMA = "schema";
	

	@Parameter(property="konig.deploy.gcp.bigquery.directory", defaultValue="${konig.deploy.gcp.directory}/bigquery")
	private File directory;

	@Parameter(property="konig.deploy.gcp.bigquery.dataset", defaultValue="${konig.deploy.gcp.bigquery.directory}/dataset")
	private File dataset;

	@Parameter(property="konig.deploy.gcp.bigquery.schema", defaultValue="${konig.deploy.gcp.bigquery.directory}/schema")
	private File schema;
	
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


	public String toString() {
		return Yaml.toString(this);
	}
	
	

}

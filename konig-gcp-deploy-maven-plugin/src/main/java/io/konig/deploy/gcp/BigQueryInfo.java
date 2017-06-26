package io.konig.deploy.gcp;

import java.io.File;

public class BigQueryInfo {
	private static final String BIGQUERY = "bigquery";
	private static final String DATASET = "dataset";
	private static final String SCHEMA = "schema";
	

	private File directory;
	private File datasets;
	private File schema;
	
	public File getDirectory() {
		return directory;
	}
	public void setDirectory(File directory) {
		this.directory = directory;
	}
	public File getDatasets() {
		return datasets;
	}
	public void setDatasets(File datasets) {
		this.datasets = datasets;
	}
	public File getSchema() {
		return schema;
	}
	public void setSchema(File schema) {
		this.schema = schema;
	}
	
	public void init(File gcpDir) {
		if (directory == null) {
			directory = new File(gcpDir, BIGQUERY);
		}
		if (datasets == null) {
			datasets = new File(directory, DATASET);
		}
		if (schema == null) {
			schema = new File(directory, SCHEMA);
		}
		
	}
	
	
	

}

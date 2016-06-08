package io.konig.schemagen.gcp;

public class BigQueryTableReference {
	
	private String projectId;
	private String datasetId;
	private String tableId;
	
	public BigQueryTableReference() {
		
	}
	
	
	public BigQueryTableReference(String projectId, String datasetId, String tableId) {
		this.projectId = projectId;
		this.datasetId = datasetId;
		this.tableId = tableId;
	}


	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
	public String getTableId() {
		return tableId;
	}
	public void setTableId(String tableId) {
		this.tableId = tableId;
	}
	
	
	

}

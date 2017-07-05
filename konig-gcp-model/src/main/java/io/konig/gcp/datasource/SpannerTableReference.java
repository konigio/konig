package io.konig.gcp.datasource;


public class SpannerTableReference {

	private String projectId;
	private String databaseName;
	private String tableName;
	
	public SpannerTableReference() {
		
	}
	
	public SpannerTableReference(String projectId, String databaseId, String tableId) {
		this.projectId = projectId;
		this.databaseName = databaseId;
		this.tableName = tableId;
	}

	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
}

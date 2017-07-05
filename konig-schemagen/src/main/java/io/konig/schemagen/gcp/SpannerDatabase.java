package io.konig.schemagen.gcp;

import java.util.ArrayList;
import java.util.List;

public class SpannerDatabase {
	
	private String databaseName;
	private GoogleCloudProject databaseProject;
	private List<SpannerTable> tableList = new ArrayList<>();
	
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	
	public GoogleCloudProject getDatabaseProject() {
		return databaseProject;
	}
	public void setDatabaseProject(GoogleCloudProject project) {
		this.databaseProject = project;
	}
	
	public void addDatasetTable(SpannerTable table) {
		tableList.add(table);
		//table.setTableDatabase(this);
	}
	
	public List<SpannerTable> getDatasetTable() {
		return tableList;
	}
	
	public SpannerTable findDatasetTable(String tableId) {
		for (SpannerTable t : tableList) {
			if (tableId.equals(t.getTableName())) {
				return t;
			}
		}
		return null;
	}
	
}

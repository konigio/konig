package io.konig.schemagen.gcp;

import java.util.ArrayList;
import java.util.List;

public class BigQueryDataset {
	
	private String datasetId;
	private GoogleCloudProject datasetProject;
	private List<BigQueryTable> tableList = new ArrayList<>();
	
	public String getDatasetId() {
		return datasetId;
	}
	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}
	
	public GoogleCloudProject getDatasetProject() {
		return datasetProject;
	}
	public void setDatasetProject(GoogleCloudProject project) {
		this.datasetProject = project;
	}
	
	public void addDatasetTable(BigQueryTable table) {
		tableList.add(table);
		table.setTableDataset(this);
	}
	
	public List<BigQueryTable> getDatasetTable() {
		return tableList;
	}
	
	public BigQueryTable findDatasetTable(String tableId) {
		for (BigQueryTable t : tableList) {
			if (tableId.equals(t.getTableId())) {
				return t;
			}
		}
		return null;
	}
	
}

package io.konig.schemagen.gcp;

import java.util.ArrayList;
import java.util.List;

public class GoogleCloudProject {

	private String projectId;
	private List<BigQueryDataset> datasetList = new ArrayList<>();
	
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public List<BigQueryDataset> getDatasetList() {
		return datasetList;
	}
	public void addDataset(BigQueryDataset dataset) {
		datasetList.add(dataset);
		dataset.setDatasetProject(this);
	}
	
	
	public BigQueryDataset dataset(String datasetId) {
		for (BigQueryDataset s : datasetList) {
			if (datasetId.equals(s.getDatasetId())) {
				return s;
			}
		}
		return null;
	}
	
	
}

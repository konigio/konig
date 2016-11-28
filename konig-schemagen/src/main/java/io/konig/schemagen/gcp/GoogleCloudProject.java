package io.konig.schemagen.gcp;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.vocab.Konig;

public class GoogleCloudProject {

	private String projectId;
	private List<BigQueryDataset> datasetList = new ArrayList<>();
	
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
	public void addProjectDataset(BigQueryDataset dataset) {
		datasetList.add(dataset);
		dataset.setDatasetProject(this);
	}
	
	
	public BigQueryDataset findProjectDataset(String datasetId) {
		for (BigQueryDataset s : datasetList) {
			if (datasetId.equals(s.getDatasetId())) {
				return s;
			}
		}
		return null;
	}
	
	
}

package io.konig.schemagen.gcp;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.vocab.Konig;

public class GoogleCloudProject {

	private String projectId;
	private List<BigQueryDataset> datasetList = new ArrayList<>();
	private List<SpannerDatabase> databaseList = new ArrayList<>();
	
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
	
	
	public List<SpannerDatabase> getProjectDatabase() {
		return databaseList;
	}
	
	
	public void addProjectDataset(BigQueryDataset dataset) {
		datasetList.add(dataset);
		dataset.setDatasetProject(this);
	}
	
	public void addProjectDatabase(SpannerDatabase database) {
		databaseList.add(database);
		database.setDatabaseProject(this);
	}
	
	
	public BigQueryDataset findProjectDataset(String datasetId) {
		for (BigQueryDataset s : datasetList) {
			if (datasetId.equals(s.getDatasetId())) {
				return s;
			}
		}
		return null;
	}
	
	public SpannerDatabase findProjectSpannerDatabase(String databaseId) {
		for (SpannerDatabase s : databaseList) {
			if (databaseId.equals(s.getDatabaseName())) {
				return s;
			}
		}
		return null;
	}
	
	
}

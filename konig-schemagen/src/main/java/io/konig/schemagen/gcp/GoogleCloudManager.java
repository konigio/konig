package io.konig.schemagen.gcp;

import java.util.Collection;

import org.openrdf.model.URI;

import io.konig.core.Vertex;

public interface GoogleCloudManager extends BigQueryTableHandler {
	
	
	void add(GoogleCloudProject project);
	GoogleCloudProject getProjectById(String id);
	
	Collection<GoogleCloudProject> listProjects();
	
	DatasetMapper getDatasetMapper();
	
	
	/**
	 * Get a collection of tables and/or views that describe a given OWL class.
	 * @param owlClass The OWL class for which BigQuery table definitions are requested.
	 * @return The collection of tables that describe the given OWL class.
	 * @throws GoogleCloudException
	 */
	Collection<BigQueryTable> tablesForClass(URI owlClass) throws GoogleCloudException;
	
	/**
	 * Get or create a BigQueryDataset that should contain a BigQuery Table for a given OWL class.
	 * @param owlClass The given OWL class.
	 * @return The BigQueryDataset for the given OWL class, or null if it should not be persisted 
	 * in BigQuery.
	 * 
	 * @throws GoogleCloudException
	 */
	BigQueryDataset datasetForClass(Vertex owlClass) throws GoogleCloudException;
	

}

package io.konig.schemagen.gcp;

import io.konig.core.Vertex;

/**
 * A simple DatasetMapper that always returns the same pre-configured value for 
 * the dataset.
 * @author Greg McFall
 *
 */
public class SimpleDatasetMapper implements DatasetMapper {

	private String datasetId;
	

	public SimpleDatasetMapper(String datasetId) {
		this.datasetId = datasetId;
	}


	@Override
	public String datasetForClass(Vertex owlClass) {
		return datasetId;
	}


	@Override
	public String getId(Vertex owlClass) {
		return datasetForClass(owlClass);
	}

}

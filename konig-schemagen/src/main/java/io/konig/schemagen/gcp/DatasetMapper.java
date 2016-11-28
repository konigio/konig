package io.konig.schemagen.gcp;

import io.konig.core.Vertex;

/**
 * An interface that maps a given OWL class to the Google Cloud Platform Dataset
 * that contains that class.
 * @author Greg McFall
 *
 */
public interface DatasetMapper {

	/**
	 * Get the Id of the Dataset that contains the given owlClass.
	 * @param owlClass The OWL class for which a dataset will be computed.
	 * @return The datasetId for the Dataset
	 */
	String datasetForClass(Vertex owlClass);
	
}

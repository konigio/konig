package io.konig.schemagen.gcp;

import java.util.Collection;

import org.openrdf.model.URI;

public interface GcpManager {
	
	/**
	 * Get a collection of tables and/or views that describe a given OWL class.
	 * @param owlClass The OWL class for which BigQuery table definitions are requested.
	 * @return The collection of tables that describe the given OWL class.
	 * @throws GoogleCloudException
	 */
	Collection<BigQueryTable> tablesForClass(URI owlClass) throws GoogleCloudException;

}

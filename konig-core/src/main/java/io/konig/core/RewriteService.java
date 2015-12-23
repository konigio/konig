package io.konig.core;

public interface RewriteService {
	
	/**
	 * Convert a canonical IRI to the local IRI for the same resource in the local server.
	 * @param iri The canonical IRI that is to be converted.
	 * @return The converted IRI
	 */
	String toLocal(String iri);
	
	/**
	 * Convert a local IRI for a resource on the local server to its canonical IRI.
	 * @param iri The local IRI for a resource on the local server.
	 * @return The canonical IRI for given IRI.
	 */
	String fromLocal(String iri);

}

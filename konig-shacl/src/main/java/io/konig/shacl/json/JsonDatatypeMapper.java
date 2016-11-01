package io.konig.shacl.json;

import org.openrdf.model.URI;

/**
 * A service that maps the URI for an RDF datatype into 
 * one of the primitive JSON datatypes.
 * @author Greg McFall
 *
 */
public interface JsonDatatypeMapper {

	/**
	 * Map the URI for an RDF datatype into one of the primitive JSON datatypes
	 * @param rdfDatatype  The URI for the RDF datatype
	 * @return The JsonDatatype corresponding to the rdfDatatype
	 */
	JsonDatatype jsonDatatype(URI rdfDatatype);
	

}

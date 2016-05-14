package io.konig.schemagen.avro;

import org.openrdf.model.URI;

import io.konig.core.Graph;

/**
 * A service that maps RDF datatypes to Avro datatypes
 * @author Greg McFall
 *
 */
public interface AvroDatatypeMapper {

	AvroDatatype toAvroDatatype(URI rdfDatatype);
}

package io.konig.schemagen.avro;

import org.openrdf.model.URI;

import io.konig.core.Vertex;
import io.konig.shacl.PropertyConstraint;

public interface AvroNamer {
	
	String toAvroNamespace(String rdfNamespace);
	
	String toAvroFullName(URI rdfName);
	
	/**
	 * Transform a SHACL Shape URI into the URI for the corresponding Avro Schema.
	 * @param shapeIRI The URI for the SHACL Shape
	 * @return The URI for the corresponding Avro Schema
	 */
	String toAvroSchemaURI(String shapeIRI);
	
	/**
	 * Construct a name for the enumeration defined by a PropertyConstraint
	 * @param recordName The fully-qualified name of the Avro record in which the property is defined.
	 * @param constraint  A PropertyConstraint containing a list of allowed values
	 * @param vertex The Vertex for the supplied constraint.
	 * @return The name for the Avro enumeration given by the PropertyConstraint
	 */
	String enumName(String recordName, PropertyConstraint constraint, Vertex vertex);
	
	

}

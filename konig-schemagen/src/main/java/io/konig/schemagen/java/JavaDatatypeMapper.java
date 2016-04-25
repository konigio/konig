package io.konig.schemagen.java;

import org.openrdf.model.URI;

import io.konig.core.Graph;

public interface JavaDatatypeMapper {

	Class<?> javaDatatype(URI datatype, Graph ontology);
	
	Class<?> primitive(Class<?> javaType);
	
}

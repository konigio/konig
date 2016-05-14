package io.konig.schemagen.java;

import org.openrdf.model.URI;

import io.konig.core.Graph;

public interface JavaDatatypeMapper {

	Class<?> javaDatatype(URI datatype);
	
	Class<?> primitive(Class<?> javaType);
	
}

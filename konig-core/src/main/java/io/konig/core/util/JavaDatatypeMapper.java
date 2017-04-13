package io.konig.core.util;

import org.openrdf.model.URI;

import io.konig.core.Graph;

public interface JavaDatatypeMapper {

	Class<?> javaDatatype(URI datatype);
	
	Class<?> primitive(Class<?> javaType);
	
}

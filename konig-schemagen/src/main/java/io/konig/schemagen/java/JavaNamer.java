package io.konig.schemagen.java;

import org.openrdf.model.URI;

public interface JavaNamer {

	/**
	 * Compute the Java class name for a given OWL class.
	 * @param owlClass The OWL class for which a Java class name is requested
	 * @return The fully qualified Java class name for the given OWL class.
	 */
	String javaClassName(URI owlClass);
	
	/**
	 * Compute the Java class name for the DataWriter that writes a given media type.
	 * @param mediaType The media type name
	 * @return The Java class name for the DataWriter that writes the given media type.
	 */
	String writerName(String mediaType);
}

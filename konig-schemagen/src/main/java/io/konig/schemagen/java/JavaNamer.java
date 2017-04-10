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
	 * Compute the name for the Java interface that describes a given OWL class.
	 * @param owlClass The OWL class for which a Java interface name is requested
	 * @return The fully qualified Java interface name for the given OWL class.
	 */
	String javaInterfaceName(URI owlClass);
	
	/**
	 * Compute the name for the Java class that implements the data writer for a given Shape.
	 * @param shapeId  The identifier for the target Shape
	 * @param format The output format of the writer.
	 * @return The name of the data writer for the given Shape in the given format.
	 */
	String writerName(URI shapeId, Format format);
	
	/**
	 * Compute the name of the Java class that implements the data reader for a 
	 * given Shape.
	 * @param shapeId The identifier for the target Shape
	 * @param format The output format of the reader
	 * @return The name of the data reader for the given Shape in the given format.
	 */
	String readerName(URI shapeId, Format format);
	
	/**
	 * Compute the Java class name for the DataWriter that writes a given media type.
	 * @param mediaType The media type name
	 * @return The Java class name for the DataWriter that writes the given media type.
	 */
	String writerName(String mediaType);
	
	/**
	 * Get the name of the Namespaces utility class which provides a method
	 * for computing CURIEs.
	 * @return The name of the Namespaces utility class
	 */
	String namespacesClass();
	
	String canonicalReaderName(URI owlClassId, Format format);
}

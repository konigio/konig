package io.konig.schemagen.java;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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

package io.konig.schemagen.avro;

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


import java.io.File;

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
	 * Get the Avro IDL file for a given Shape.
	 * @param shapeIRI The IRI for the Shape for which the corresponding Avro IDL file is requested
	 * @return The Avro IDL file for the given Shape
	 */
	File idlFile(URI shapeIRI);
	
	/**
	 * Construct a name for the enumeration defined by a PropertyConstraint
	 * @param recordName The fully-qualified name of the Avro record in which the property is defined.
	 * @param constraint  A PropertyConstraint containing a list of allowed values
	 * @return The name for the Avro enumeration given by the PropertyConstraint
	 */
	String enumName(String recordName, PropertyConstraint constraint);
	
	String valueShapeName(String recordName, PropertyConstraint constraint);
	
	

}

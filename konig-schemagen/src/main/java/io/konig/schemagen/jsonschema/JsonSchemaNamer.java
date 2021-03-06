package io.konig.schemagen.jsonschema;

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


import io.konig.shacl.Shape;

public interface JsonSchemaNamer {
	
	/**
	 * Compute the id for the JSON Schema associated with a given SHACL Shape.
	 * @param shape The Shape that is associated with the JSON Schema.
	 * @return The fully-qualified URL for the JSON Schema
	 */
	String schemaId(Shape shape);
	
	/**
	 * Compute the name of the file in which the JSON Schema for a given Shape will be stored.
	 * @param shape The Shape whose JSON Schema is to be stored in a file.
	 * @return The name of the file that will store the JSON Schema for the given Shape
	 */
	String jsonSchemaFileName(Shape shape);
	

}

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


import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.shacl.Shape;

/**
 * A listener that is notified when the JSON Schema for a Shape is generated.
 * @author Greg McFall
 *
 */
public interface JsonSchemaListener {
	
	/**
	 * Receive notification that the JSON Schema for a Shape has been generated
	 * @param shape The Shape for which a JSON Schema was generated
	 * @param schema The JSON Schema specification
	 */
	public void handleJsonSchema(Shape shape, ObjectNode schema);
}

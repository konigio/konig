package io.konig.shacl.json;

/*
 * #%L
 * Konig SHACL
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Vertex;
import io.konig.shacl.Shape;

/**
 * An interface for serializing a resource that conforms to a specific Shape as a JSON object.
 * @author Greg McFall
 *
 */
public interface JsonShapeWriter {
	
	/**
	 * Serialize a resource that conforms to a specific Shape as a JSON object
	 * @param subject The resource to be serialized
	 * @param shape The shape of the resource
	 * @param json The JSON generator used to serialize the resource
	 * @throws IOException
	 */
	public void toJson(Vertex subject, Shape shape, JsonGenerator json) throws IOException;
	
	/**
	 * Serialize a resource that conforms to a specific Shape as a Jackson ObjectNode.
	 * @param subject  The resource to be serialized
	 * @param shape The shape of the resource
	 * @return A Jackson ObjectNode representing the JSON structure
	 * @throws IOException 
	 */
	public ObjectNode toJson(Vertex subject, Shape shape) throws IOException;

}

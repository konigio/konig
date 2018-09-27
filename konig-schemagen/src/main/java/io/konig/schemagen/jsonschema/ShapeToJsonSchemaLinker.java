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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Graph;
import io.konig.core.vocab.Konig;
import io.konig.shacl.Shape;

public class ShapeToJsonSchemaLinker implements JsonSchemaListener {
	
	private Graph graph;
	

	public ShapeToJsonSchemaLinker(Graph graph) {
		this.graph = graph;
	}


	@Override
	public void handleJsonSchema(Shape shape, ObjectNode schema) {
		
		Resource shapeId = shape.getId();
		JsonNode idNode = schema.get("id");
		if (idNode != null) {
			URI schemaId = new URIImpl(idNode.asText());
			
			shape.setPreferredJsonSchema(schemaId);
			graph.edge(shapeId, Konig.preferredJsonSchema, schemaId);
		}
	}

}

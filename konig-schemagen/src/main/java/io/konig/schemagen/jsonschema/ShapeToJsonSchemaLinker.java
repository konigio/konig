package io.konig.schemagen.jsonschema;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

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
		URI schemaId = new URIImpl(schema.get("id").asText());
		
		graph.edge(shapeId, Konig.jsonSchemaRendition, schemaId);
	}

}

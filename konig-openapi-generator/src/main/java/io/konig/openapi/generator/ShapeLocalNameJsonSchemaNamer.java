package io.konig.openapi.generator;

import org.openrdf.model.URI;

import io.konig.jsonschema.generator.JsonSchemaNamer;
import io.konig.shacl.Shape;

public class ShapeLocalNameJsonSchemaNamer implements JsonSchemaNamer {


	@Override
	public String schemaId(Shape shape) {
		
		return	((URI) shape.getId()).getLocalName();
	}

}

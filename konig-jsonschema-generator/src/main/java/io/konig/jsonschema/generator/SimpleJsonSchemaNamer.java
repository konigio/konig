package io.konig.jsonschema.generator;

import io.konig.shacl.Shape;

public class SimpleJsonSchemaNamer implements JsonSchemaNamer {
	
	private String idSuffix;
	


	public SimpleJsonSchemaNamer(String idSuffix) {
		this.idSuffix = idSuffix;
	}



	@Override
	public String schemaId(Shape shape) {
		String shapeId = shape.getId().stringValue();
		return shapeId + idSuffix;
	}



}

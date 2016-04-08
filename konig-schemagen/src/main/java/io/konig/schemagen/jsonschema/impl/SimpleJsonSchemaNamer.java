package io.konig.schemagen.jsonschema.impl;

import io.konig.schemagen.jsonschema.JsonSchemaNamer;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeMediaTypeNamer;

public class SimpleJsonSchemaNamer implements JsonSchemaNamer {
	
	private String idSuffix;
	private ShapeMediaTypeNamer mediaTypeNamer;
	


	public SimpleJsonSchemaNamer(String idSuffix, ShapeMediaTypeNamer mediaTypeNamer) {
		this.idSuffix = idSuffix;
		this.mediaTypeNamer = mediaTypeNamer;
	}



	@Override
	public String schemaId(Shape shape) {
		String shapeId = shape.getId().stringValue();
		return shapeId + idSuffix;
	}



	@Override
	public String jsonSchemaFileName(Shape shape) {
		String mediaTypeName = mediaTypeNamer.baseMediaTypeName(shape);
		int slash = mediaTypeName.indexOf('/');
		return mediaTypeName.substring(slash+1);
	}

}

package io.konig.openapi.model;

import io.konig.jsonschema.model.JsonSchema;

public class MediaType {
	
	private String stringValue;
	private JsonSchema schema;
	

	public MediaType(String value) {
		this.stringValue = value;
	}


	public JsonSchema getSchema() {
		return schema;
	}


	public void setSchema(JsonSchema schema) {
		this.schema = schema;
	}
	
	public String stringValue() {
		return stringValue;
	}

}

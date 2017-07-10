package io.konig.openapi.model;

import io.konig.jsonschema.model.JsonSchema;
import io.konig.yaml.Yaml;

public class Components {

	private SchemaMap schemas;

	public SchemaMap getSchemas() {
		return schemas;
	}
	
	public void addSchema(JsonSchema schema) {
		if (schemas == null) {
			schemas = new SchemaMap();
		}
		schemas.put(schema.getRef(), schema);
	}

	public void setSchemas(SchemaMap schemas) {
		this.schemas = schemas;
	}
	
	public String toString() {
		return Yaml.toString(this);
	}

}

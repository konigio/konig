package io.konig.openapi.model;

import io.konig.yaml.Yaml;

public class Components {

	private SchemaMap schemas;

	public SchemaMap getSchemas() {
		return schemas;
	}

	public void setSchemas(SchemaMap schemas) {
		this.schemas = schemas;
	}
	
	public String toString() {
		return Yaml.toString(this);
	}

}

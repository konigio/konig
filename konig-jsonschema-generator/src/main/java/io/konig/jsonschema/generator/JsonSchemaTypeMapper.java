package io.konig.jsonschema.generator;

import io.konig.jsonschema.model.JsonSchemaDatatype;
import io.konig.shacl.PropertyConstraint;

public interface JsonSchemaTypeMapper {

	JsonSchemaDatatype type(PropertyConstraint property);
	
}

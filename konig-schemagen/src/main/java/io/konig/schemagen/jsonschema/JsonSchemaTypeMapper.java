package io.konig.schemagen.jsonschema;

import io.konig.shacl.PropertyConstraint;

public interface JsonSchemaTypeMapper {

	JsonSchemaDatatype type(PropertyConstraint property);
	
}

package io.konig.schemagen.jsonschema.impl;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.schemagen.jsonschema.JsonSchemaDatatype;
import io.konig.schemagen.jsonschema.JsonSchemaTypeMapper;
import io.konig.shacl.PropertyConstraint;

public class SimpleJsonSchemaTypeMapper implements JsonSchemaTypeMapper {

	@Override
	public JsonSchemaDatatype type(PropertyConstraint property) {
		URI rdfDatatype = property.getDatatype();
		

		if (XMLSchema.STRING.equals(rdfDatatype)) {
			return JsonSchemaDatatype.STRING;
		}
		
		if (XMLSchema.ANYURI.equals(rdfDatatype)) {
			return JsonSchemaDatatype.STRING;
		}
		
		if (XMLSchema.BOOLEAN.equals(rdfDatatype)) {
			return JsonSchemaDatatype.BOOLEAN;
		}
		
		if (XMLSchema.BOOLEAN.equals(rdfDatatype)) {
			return JsonSchemaDatatype.BOOLEAN;
		}
		
		
		if (XMLSchema.DATE.equals(rdfDatatype)) {
			return JsonSchemaDatatype.DATE;
		}
		
		if (XMLSchema.DATETIME.equals(rdfDatatype)) {
			return JsonSchemaDatatype.TIMESTAMP;
		}
		
		if (XMLSchema.DOUBLE.equals(rdfDatatype)) {
			return JsonSchemaDatatype.DOUBLE;
		}
		
		if (XMLSchema.DECIMAL.equals(rdfDatatype)) {
			return JsonSchemaDatatype.DOUBLE;
		}
		
		if (XMLSchema.FLOAT.equals(rdfDatatype)) {
			return JsonSchemaDatatype.FLOAT;
		}

		if (XMLSchema.INT.equals(rdfDatatype)) {
			return JsonSchemaDatatype.INT;
		}
		
		if (XMLSchema.INTEGER.equals(rdfDatatype)) {
			return JsonSchemaDatatype.LONG;
		}
		
		if (XMLSchema.NEGATIVE_INTEGER.equals(rdfDatatype)) {
			return JsonSchemaDatatype.LONG;
		}
		
		if (XMLSchema.NON_NEGATIVE_INTEGER.equals(rdfDatatype)) {
			return JsonSchemaDatatype.LONG;
		}

		if (XMLSchema.NON_POSITIVE_INTEGER.equals(rdfDatatype)) {
			return JsonSchemaDatatype.LONG;
		}

		if (XMLSchema.NORMALIZEDSTRING.equals(rdfDatatype)) {
			return JsonSchemaDatatype.STRING;
		}
		
		if (XMLSchema.POSITIVE_INTEGER.equals(rdfDatatype)) {
			return JsonSchemaDatatype.LONG;
		}
		
		if (XMLSchema.SHORT.equals(rdfDatatype)) {
			return JsonSchemaDatatype.INT;
		}
		
		if (XMLSchema.TIME.equals(rdfDatatype)) {
			return JsonSchemaDatatype.TIME;
		}
		
		if (XMLSchema.TOKEN.equals(rdfDatatype)) {
			return JsonSchemaDatatype.STRING;
		}
		
		return null;
		
	}

}

package io.konig.jsonschema.generator;

/*
 * #%L
 * Konig JSON Schema Generator
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.jsonschema.model.JsonSchemaDatatype;
import io.konig.shacl.PropertyConstraint;

public class SimpleJsonSchemaTypeMapper implements JsonSchemaTypeMapper {

	@Override
	public JsonSchemaDatatype type(PropertyConstraint property) {
		JsonSchemaDatatype result = null;
		URI rdfDatatype = property.getDatatype();
		

		if (XMLSchema.STRING.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.STRING;
		} else if (XMLSchema.ANYURI.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.STRING;
		} else if (XMLSchema.BOOLEAN.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.BOOLEAN;
		} else if (XMLSchema.BYTE.equals(rdfDatatype)) {
			result =  JsonSchemaDatatype.INT;
		} else if (XMLSchema.DAYTIMEDURATION.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.STRING;
		} else if (XMLSchema.DATE.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.DATE;
		} else if (XMLSchema.DATETIME.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.TIMESTAMP;
		} else if (XMLSchema.DOUBLE.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.DOUBLE;
		} else if (XMLSchema.DECIMAL.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.DOUBLE;
		} else if (XMLSchema.FLOAT.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.FLOAT;
		} else if (XMLSchema.INT.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.INT;
		} else if (XMLSchema.INTEGER.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.LONG;
		} else if (XMLSchema.LONG.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.LONG;
		} else if (XMLSchema.NEGATIVE_INTEGER.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.LONG;
		} else if (XMLSchema.NON_NEGATIVE_INTEGER.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.LONG;
		} else if (XMLSchema.NON_POSITIVE_INTEGER.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.LONG;
		} else if (XMLSchema.NORMALIZEDSTRING.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.STRING;
		} else if (XMLSchema.POSITIVE_INTEGER.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.LONG;
		} else if (XMLSchema.SHORT.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.INT;
		} else if (XMLSchema.TIME.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.TIME;
		} else if (XMLSchema.TOKEN.equals(rdfDatatype)) {
			result = JsonSchemaDatatype.STRING;
		}
		if (result != null) {

			Integer maxCount = property.getMaxCount();
			Number maxExclusive = property.getMaxExclusive();
			Number maxInclusive = property.getMaxInclusive();
			Number minExclusive = property.getMinInclusive();
			Number minInclusive = property.getMinInclusive();
			
			if (
				maxCount!=null || maxExclusive!=null || maxInclusive!=null ||
				minExclusive!=null || minInclusive!=null
			) {
				String typeName = result.getType();
				String format = result.getFormat();
				Number minimum = null;
				Number maximum = null;
				Boolean exclusiveMaximum=null;
				Boolean exclusiveMinimum=null;
				
				if (minInclusive != null) {
					minimum = minInclusive;
				} else if (minExclusive != null) {
					minimum = minExclusive;
					exclusiveMinimum = Boolean.TRUE;
				}
				
				if (maxInclusive!=null) {
					maximum = maxInclusive;
				} else if (maxExclusive!=null) {
					maximum = maxExclusive;
					exclusiveMaximum = Boolean.TRUE;
				}
				
				result = new JsonSchemaDatatype(typeName, format, exclusiveMinimum, exclusiveMaximum, minimum, maximum);
			}
		}
		return result;
		
	}

}

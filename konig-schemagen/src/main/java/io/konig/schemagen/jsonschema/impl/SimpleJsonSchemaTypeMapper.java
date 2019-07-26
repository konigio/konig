package io.konig.schemagen.jsonschema.impl;

/*
 * #%L
 * Konig Schema Generator
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
import org.openrdf.model.vocabulary.GEO;
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
		
		if (XMLSchema.BYTE.equals(rdfDatatype)) {
			return JsonSchemaDatatype.INT;
		}
		
		if (XMLSchema.DAYTIMEDURATION.equals(rdfDatatype)) {
			return JsonSchemaDatatype.STRING;
		}
		if (GEO.WKT_LITERAL.equals(rdfDatatype)) {
			return JsonSchemaDatatype.STRING;
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
		if (XMLSchema.GYEAR.equals(rdfDatatype)) {
			return JsonSchemaDatatype.INT;
		}

		if (XMLSchema.INT.equals(rdfDatatype)) {
			return JsonSchemaDatatype.INT;
		}
		
		if (XMLSchema.INTEGER.equals(rdfDatatype)) {
			return JsonSchemaDatatype.LONG;
		}

		
		if (XMLSchema.LONG.equals(rdfDatatype)) {
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

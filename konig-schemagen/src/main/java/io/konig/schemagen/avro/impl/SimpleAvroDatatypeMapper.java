package io.konig.schemagen.avro.impl;

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
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.schemagen.avro.AvroDatatype;
import io.konig.schemagen.avro.AvroDatatypeMapper;

public class SimpleAvroDatatypeMapper implements AvroDatatypeMapper {

	@Override
	public AvroDatatype toAvroDatatype(URI rdfDatatype) {
		
		if (XMLSchema.STRING.equals(rdfDatatype)) {
			return AvroDatatype.STRING;
		}
		
		if (XMLSchema.ANYURI.equals(rdfDatatype)) {
			return AvroDatatype.STRING;
		}
		
		if (XMLSchema.BOOLEAN.equals(rdfDatatype)) {
			return AvroDatatype.BOOLEAN;
		}
		
		if (XMLSchema.BOOLEAN.equals(rdfDatatype)) {
			return AvroDatatype.BOOLEAN;
		}
		
		if (XMLSchema.BYTE.equals(rdfDatatype)) {
			return AvroDatatype.BYTE;
		}
		
		if (XMLSchema.DATE.equals(rdfDatatype)) {
			return AvroDatatype.DATE;
		}
		
		if (XMLSchema.DATETIME.equals(rdfDatatype)) {
			return AvroDatatype.TIMESTAMP;
		}
		
		if (XMLSchema.DAYTIMEDURATION.equals(rdfDatatype)) {
			return AvroDatatype.DURATION;
		}
		
		if (XMLSchema.DOUBLE.equals(rdfDatatype)) {
			return AvroDatatype.DOUBLE;
		}
		
		if (XMLSchema.DECIMAL.equals(rdfDatatype)) {
			return AvroDatatype.DOUBLE;
		}
		
		if (XMLSchema.FLOAT.equals(rdfDatatype)) {
			return AvroDatatype.FLOAT;
		}

		if (XMLSchema.INT.equals(rdfDatatype)) {
			return AvroDatatype.INT;
		}
		
		if (XMLSchema.INTEGER.equals(rdfDatatype)) {
			return AvroDatatype.LONG;
		}

		
		if (XMLSchema.LONG.equals(rdfDatatype)) {
			return AvroDatatype.LONG;
		}
		
		if (XMLSchema.NEGATIVE_INTEGER.equals(rdfDatatype)) {
			return AvroDatatype.LONG;
		}
		
		if (XMLSchema.NON_NEGATIVE_INTEGER.equals(rdfDatatype)) {
			return AvroDatatype.LONG;
		}

		if (XMLSchema.NON_POSITIVE_INTEGER.equals(rdfDatatype)) {
			return AvroDatatype.LONG;
		}

		if (XMLSchema.NORMALIZEDSTRING.equals(rdfDatatype)) {
			return AvroDatatype.STRING;
		}
		
		if (XMLSchema.POSITIVE_INTEGER.equals(rdfDatatype)) {
			return AvroDatatype.LONG;
		}
		
		if (XMLSchema.SHORT.equals(rdfDatatype)) {
			return AvroDatatype.INT;
		}
		
		if (XMLSchema.TIME.equals(rdfDatatype)) {
			return AvroDatatype.TIME;
		}
		
		if (XMLSchema.TOKEN.equals(rdfDatatype)) {
			return AvroDatatype.STRING;
		}
		
		return null;
	}

}

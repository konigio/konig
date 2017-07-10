package io.konig.gae.datastore.impl;

/*
 * #%L
 * Konig GAE Datastore
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


import java.util.GregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import static io.konig.gae.datastore.DatastoreDatatype.*;

import io.konig.gae.datastore.DatastoreDatatype;
import io.konig.gae.datastore.DatastoreDatatypeMapper;

public class SimpleDatastoreDatatypeMapper implements DatastoreDatatypeMapper {
	private static final int MAX_STRING_LENGTH = 1500;

	public SimpleDatastoreDatatypeMapper() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public DatastoreDatatype getDatastoreDatatype(Literal literal) {
		
		URI rdfDatatype = literal.getDatatype();
		String value = literal.getLabel();

		if (XMLSchema.STRING.equals(rdfDatatype)) {
			return (value.length() < MAX_STRING_LENGTH) ?
					STRING : TEXT;
		}
		
		if (XMLSchema.ANYURI.equals(rdfDatatype)) {
			return STRING;
		}
		
		if (XMLSchema.BOOLEAN.equals(rdfDatatype)) {
			return BOOLEAN;
		}
		
		if (XMLSchema.BYTE.equals(rdfDatatype)) {
			return SHORT_BLOB;
		}
		
		if (XMLSchema.DATE.equals(rdfDatatype)) {
			return DATE;
		}
		
		if (XMLSchema.DATETIME.equals(rdfDatatype)) {
			return DATE;
		}
		
		if (XMLSchema.DAYTIMEDURATION.equals(rdfDatatype)) {
			return STRING;
		}
		
		if (XMLSchema.DOUBLE.equals(rdfDatatype)) {
			return DOUBLE;
		}
		
		if (XMLSchema.DECIMAL.equals(rdfDatatype)) {
			return DOUBLE;
		}
		
		if (XMLSchema.FLOAT.equals(rdfDatatype)) {
			return DOUBLE;
		}

		if (XMLSchema.INT.equals(rdfDatatype)) {
			return LONG;
		}
		
		if (XMLSchema.INTEGER.equals(rdfDatatype)) {
			return LONG;
		}
		
		if (XMLSchema.NEGATIVE_INTEGER.equals(rdfDatatype)) {
			return LONG;
		}
		
		if (XMLSchema.NON_NEGATIVE_INTEGER.equals(rdfDatatype)) {
			return LONG;
		}

		if (XMLSchema.NON_POSITIVE_INTEGER.equals(rdfDatatype)) {
			return LONG;
		}

		if (XMLSchema.NORMALIZEDSTRING.equals(rdfDatatype)) {
			return (value.length() < MAX_STRING_LENGTH) ? STRING : TEXT;
		}
		
		if (XMLSchema.POSITIVE_INTEGER.equals(rdfDatatype)) {
			return LONG;
		}
		
		if (XMLSchema.SHORT.equals(rdfDatatype)) {
			return LONG;
		}
		
		if (XMLSchema.TIME.equals(rdfDatatype)) {
			return DATE;
		}
		
		if (XMLSchema.TOKEN.equals(rdfDatatype)) {
			return STRING;
		}
		
		return null;
	}

}

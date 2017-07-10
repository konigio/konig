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

import io.konig.core.DatatypeRestriction;
import io.konig.core.OwlReasoner;
import io.konig.schemagen.avro.AvroDatatype;
import io.konig.schemagen.avro.AvroDatatypeMapper;

public class SmartAvroDatatypeMapper implements AvroDatatypeMapper {
	
	private OwlReasoner reasoner;
	private SimpleAvroDatatypeMapper simple = new SimpleAvroDatatypeMapper();

	public SmartAvroDatatypeMapper(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}

	@Override
	public AvroDatatype toAvroDatatype(URI rdfDatatype) {
		
		if (rdfDatatype.getNamespace().equals(XMLSchema.NAMESPACE)) {
			return simple.toAvroDatatype(rdfDatatype);
		}
		
		DatatypeRestriction r = reasoner.datatypeRestriction(rdfDatatype);
		
		URI datatype = r.getOnDatatype();
		if (datatype != null && XMLSchema.NAMESPACE.equals(datatype.getNamespace())) {
			return simple.toAvroDatatype(datatype);
		}
		
		return null;
	}

}

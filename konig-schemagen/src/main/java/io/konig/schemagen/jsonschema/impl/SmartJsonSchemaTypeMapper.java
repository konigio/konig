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

import io.konig.core.DatatypeRestriction;
import io.konig.core.OwlReasoner;
import io.konig.schemagen.jsonschema.JsonSchemaDatatype;
import io.konig.schemagen.jsonschema.JsonSchemaTypeMapper;
import io.konig.shacl.PropertyConstraint;

public class SmartJsonSchemaTypeMapper implements JsonSchemaTypeMapper {
	
	private OwlReasoner reasoner;
	private SimpleJsonSchemaTypeMapper simple = new SimpleJsonSchemaTypeMapper();
	
	

	public SmartJsonSchemaTypeMapper(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}


	@Override
	public JsonSchemaDatatype type(PropertyConstraint property) {
		
		URI datatype = property.getDatatype();
		
		if (datatype != null) {
			if (XMLSchema.NAMESPACE.equals(datatype.getNamespace())) {
				return simple.type(property);
			} else if (GEO.WKT_LITERAL.equals(datatype)) {
				return JsonSchemaDatatype.STRING;
			
			} else {
				DatatypeRestriction r = reasoner.datatypeRestriction(datatype);
				URI onDatatype = r.getOnDatatype();
				
				if (onDatatype != null) {
					if (XMLSchema.NAMESPACE.equals(onDatatype.getNamespace())) {
						PropertyConstraint p = new PropertyConstraint(property.getPredicate());
						p.setDatatype(onDatatype);
						JsonSchemaDatatype j = simple.type(p);
						if (j != null) {
							String typeName = j.getTypeName();
							String format = j.getFormat();
							Boolean exclusiveMinimum = null;
							Boolean exclusiveMaximum = null;
							Number maximum = r.getMaxExclusive();
							Number minimum = r.getMinExclusive();
							if (maximum == null) {
								maximum = r.getMaxInclusive();
							} else {
								exclusiveMaximum = true;
							}
							if (minimum == null) {
								minimum = r.getMinInclusive();
							} else {
								exclusiveMinimum = true;
							}
							
							return new JsonSchemaDatatype(typeName, format, exclusiveMinimum, exclusiveMaximum, minimum, maximum);
						}
					}
				}
				
			}
		}
		
		return null;
	}

}

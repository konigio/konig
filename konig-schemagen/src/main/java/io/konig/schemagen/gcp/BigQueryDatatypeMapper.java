package io.konig.schemagen.gcp;

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

import io.konig.core.OwlReasoner;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

public class BigQueryDatatypeMapper {
	
	private OwlReasoner owlReasoner = new OwlReasoner(null);

	public BigQueryDatatype type(PropertyConstraint c) {
		
		URI datatype = c.getDatatype();
		
		if (datatype != null) {

			if (owlReasoner.isRealNumber(datatype)) {
				return BigQueryDatatype.FLOAT64;
			}
			
			if (owlReasoner.isIntegerDatatype(datatype)) {
				return BigQueryDatatype.INT64;
			}
			
			if (XMLSchema.STRING.equals(datatype)) {
				return BigQueryDatatype.STRING;
			}
			
			if (
				XMLSchema.DATETIME.equals(datatype) ||
				XMLSchema.DATE.equals(datatype) 
			) {
				return BigQueryDatatype.TIMESTAMP;
			}
		}
		
		if (c.getNodeKind() == NodeKind.IRI) {
			return BigQueryDatatype.STRING;
		}
		if (c.getShapeId() != null) {
			return BigQueryDatatype.RECORD;
		}
		
		return BigQueryDatatype.STRING;
	}
}

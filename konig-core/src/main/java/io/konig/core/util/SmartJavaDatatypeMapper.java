package io.konig.core.util;

/*
 * #%L
 * Konig Core
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

public class SmartJavaDatatypeMapper extends BasicJavaDatatypeMapper {
	
	private OwlReasoner reasoner;

	public SmartJavaDatatypeMapper(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}

	@Override
	public Class<?> javaDatatype(URI datatype) {
		if (XMLSchema.NAMESPACE.equals(datatype.getNamespace())) {
			return super.javaDatatype(datatype);
		} else {
			DatatypeRestriction r = reasoner.datatypeRestriction(datatype);
			if (r != null) {
				URI type = r.getOnDatatype();
				if (type != null && XMLSchema.NAMESPACE.equals(type.getNamespace())) {
					return super.javaDatatype(type);
				}
			}
		}
		throw new RuntimeException("Java datatype not found: " + datatype);
	}

	@Override
	public Class<?> primitive(Class<?> javaType) {
		return super.primitive(javaType);
	}

}

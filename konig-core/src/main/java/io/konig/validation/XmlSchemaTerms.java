package io.konig.validation;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

public class XmlSchemaTerms {
	private static Set<URI> terms;
	
	public static boolean isXmlSchemaTerm(URI term) {
		if (term.getNamespace().equals(XMLSchema.NAMESPACE)) {
			Set<URI> termSet = termSet();
			return termSet.contains(term);
		}
		return false;
	}

	private static Set<URI> termSet() {
		if (terms == null) {
			terms = new HashSet<>();
			Field[] fieldList = XMLSchema.class.getDeclaredFields();
			for (Field field : fieldList) {
				if (
						Modifier.isStatic(field.getModifiers()) && 
						Modifier.isPublic(field.getModifiers()) &&
						URI.class.isAssignableFrom(field.getType())
				) {
					try {
						terms.add((URI) field.get(null));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return terms;
	}

}

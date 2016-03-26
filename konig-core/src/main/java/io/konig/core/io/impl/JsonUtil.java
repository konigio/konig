package io.konig.core.io.impl;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import java.util.Collection;

import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.impl.BasicContext;

public class JsonUtil {
	
	public static Context createContext(Collection<Namespace> set) {
		Context context = new BasicContext(null);
		for (Namespace ns : set) {
			context.add(new Term(ns.getPrefix(), ns.getName(), Term.Kind.NAMESPACE));
		}
		
		return context;
	}
	
	public static boolean isInteger(Literal literal) {
		URI datatype = literal.getDatatype();
		return datatype!=null && (
				XMLSchema.INT.equals(datatype) ||
				XMLSchema.BYTE.equals(datatype) ||
				XMLSchema.INTEGER.equals(datatype) ||
				XMLSchema.LONG.equals(datatype)
			);
	}
	
	public static boolean isRealNumber(Literal literal) {
		URI datatype = literal.getDatatype();
		return datatype!=null && (
				XMLSchema.FLOAT.equals(datatype) ||
				XMLSchema.DOUBLE.equals(datatype) ||
				XMLSchema.DECIMAL.equals(datatype)
			);
	}
	
	public static boolean isBoolean(Literal literal) {

		URI datatype = literal.getDatatype();
		return datatype!=null && XMLSchema.BOOLEAN.equals(datatype);
	}
	
	public static Object toObject(Literal literal) {
		
		String value = literal.stringValue();
		if (isInteger(literal)) {
			return new Long(value);
		}
		if (isRealNumber(literal)) {
			return new Double(value);
		}
		if (isBoolean(literal)) {
			return new Boolean("true".equalsIgnoreCase(value));
		}
		
		return value;
	}
}

package io.konig.core.pojo.impl;

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


import java.lang.reflect.Method;
import java.math.BigInteger;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

public class NumberValueHandler extends LiteralPropertyHandler {

	public NumberValueHandler(Method setter) {
		super(setter);
	}

	@Override
	protected Object javaValue(Literal literal) {
		if (literal == null) {
			return null;
		}
		String text = literal.stringValue();
		URI datatype = literal.getDatatype();
		if (XMLSchema.FLOAT.equals(datatype)) {
			return new Float(text);
		}
		if (XMLSchema.DOUBLE.equals(datatype) || text.indexOf('.')>=0 || text.indexOf('e')>=0 || text.indexOf('E')>=0) {
			return new Double(text);
		}
		
		if (XMLSchema.LONG.equals(datatype)) {
			return new Long(text);
		}
		
		if (XMLSchema.INT.equals(datatype)) {
			return new Integer(text);
		}
		if (XMLSchema.SHORT.equals(datatype)) {
			return new Short(text);
		}
		if (XMLSchema.BYTE.equals(datatype)) {
			return new Byte(text);
		}
		
		try {
			long value = Long.parseLong(text);
			if (value < Integer.MAX_VALUE && value > Integer.MIN_VALUE) {
				return new Integer((int)value);
			}
			return new Long(value);
			
		} catch (Throwable ignore) {
			return new BigInteger(text);
		}
		
	}

}

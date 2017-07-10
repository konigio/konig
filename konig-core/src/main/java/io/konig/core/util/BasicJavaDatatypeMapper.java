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


import java.util.GregorianCalendar;

import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

public class BasicJavaDatatypeMapper implements JavaDatatypeMapper {

	@Override
	public Class<?> javaDatatype(URI rdfDatatype) {

		
		if (XMLSchema.STRING.equals(rdfDatatype)) {
			return String.class;
		}
		
		if (XMLSchema.ANYURI.equals(rdfDatatype)) {
			return URI.class;
		}
		
		if (XMLSchema.BOOLEAN.equals(rdfDatatype)) {
			return Boolean.class;
		}
		
		if (XMLSchema.BYTE.equals(rdfDatatype)) {
			return Byte.class;
		}
		
		if (XMLSchema.DATE.equals(rdfDatatype)) {
			return GregorianCalendar.class;
		}
		
		if (XMLSchema.DATETIME.equals(rdfDatatype)) {
			return GregorianCalendar.class;
		}
		
		if (XMLSchema.DAYTIMEDURATION.equals(rdfDatatype)) {
			return Duration.class;
		}
		
		if (XMLSchema.DOUBLE.equals(rdfDatatype)) {
			return Double.class;
		}
		
		if (XMLSchema.DECIMAL.equals(rdfDatatype)) {
			return Double.class;
		}
		
		if (XMLSchema.FLOAT.equals(rdfDatatype)) {
			return Float.class;
		}

		if (XMLSchema.INT.equals(rdfDatatype)) {
			return Integer.class;
		}
		
		if (XMLSchema.LONG.equals(rdfDatatype)) {
			return Long.class;
		}
		
		if (XMLSchema.INTEGER.equals(rdfDatatype)) {
			return Long.class;
		}
		
		if (XMLSchema.NEGATIVE_INTEGER.equals(rdfDatatype)) {
			return Long.class;
		}
		
		if (XMLSchema.NON_NEGATIVE_INTEGER.equals(rdfDatatype)) {
			return Long.class;
		}

		if (XMLSchema.NON_POSITIVE_INTEGER.equals(rdfDatatype)) {
			return Long.class;
		}

		if (XMLSchema.NORMALIZEDSTRING.equals(rdfDatatype)) {
			return String.class;
		}
		
		if (XMLSchema.POSITIVE_INTEGER.equals(rdfDatatype)) {
			return Long.class;
		}
		
		if (XMLSchema.SHORT.equals(rdfDatatype)) {
			return Short.class;
		}
		
		if (XMLSchema.TIME.equals(rdfDatatype)) {
			return LocalTime.class;
		}
		
		if (XMLSchema.TOKEN.equals(rdfDatatype)) {
			return String.class;
		}
		
		return null;
	}

	@Override
	public Class<?> primitive(Class<?> javaType) {
		if (javaType == Integer.class) {
			return int.class;
		}
		if (javaType == Double.class) {
			return double.class;
		}
		if (javaType == Byte.class) {
			return byte.class;
		}
		if (javaType == Short.class) {
			return short.class;
		}
		if (javaType == Long.class) {
			return long.class;
		}
		return javaType;
	}

}

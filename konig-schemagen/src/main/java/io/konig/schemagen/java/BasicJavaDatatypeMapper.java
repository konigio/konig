package io.konig.schemagen.java;

import java.util.GregorianCalendar;

import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;

public class BasicJavaDatatypeMapper implements JavaDatatypeMapper {

	@Override
	public Class<?> javaDatatype(URI rdfDatatype, Graph ontology) {

		
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
		
		if (XMLSchema.DECIMAL.equals(rdfDatatype)) {
			return Double.class;
		}
		
		if (XMLSchema.FLOAT.equals(rdfDatatype)) {
			return Float.class;
		}

		if (XMLSchema.INT.equals(rdfDatatype)) {
			return Integer.class;
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

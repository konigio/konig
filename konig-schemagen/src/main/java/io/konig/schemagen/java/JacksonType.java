package io.konig.schemagen.java;

import org.joda.time.format.ISODateTimeFormat;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JStatement;
import com.sun.codemodel.JVar;

public abstract class JacksonType {
	
	public static final JacksonType NUMBER = new NumberType();
	public static final JacksonType STRING = new StringType();
	public static final JacksonType DATE = new DateType();
	public static final JacksonType DATETIME = new DatetimeType();

	
	public abstract JStatement writeTypeField(JCodeModel model, JVar generator, String fieldName, JVar fieldValue);
	public abstract JStatement writeType(JCodeModel model, JVar generator, JVar value);
	

	public static JacksonType type(URI datatype) {
		
		if (
			XMLSchema.DECIMAL.equals(datatype) ||
			XMLSchema.FLOAT.equals(datatype) ||
			XMLSchema.DOUBLE.equals(datatype) ||
			XMLSchema.INTEGER.equals(datatype) ||
			XMLSchema.POSITIVE_INTEGER.equals(datatype) ||
			XMLSchema.NEGATIVE_INTEGER.equals(datatype) ||
			XMLSchema.NON_POSITIVE_INTEGER.equals(datatype) ||
			XMLSchema.NON_NEGATIVE_INTEGER.equals(datatype) ||
			XMLSchema.LONG.equals(datatype) ||
			XMLSchema.INT.equals(datatype) ||
			XMLSchema.SHORT.equals(datatype) ||
			XMLSchema.BYTE.equals(datatype) ||
			XMLSchema.UNSIGNED_LONG.equals(datatype) ||
			XMLSchema.UNSIGNED_INT.equals(datatype) ||
			XMLSchema.UNSIGNED_SHORT.equals(datatype) ||
			XMLSchema.UNSIGNED_BYTE.equals(datatype) 
		) {
			return NUMBER;
		}
		
		if (XMLSchema.DATETIME.equals(datatype)) {
			return DATETIME;
		}
		
		if (XMLSchema.DATE.equals(datatype)) {
			return DATE;
		}
		
		return STRING;
	}
	
	
	static class DatetimeType extends JacksonType {

		@Override
		public JStatement writeTypeField(JCodeModel model, JVar generator, String fieldName, JVar fieldValue) {
			
			JClass timeFormat = model.ref(ISODateTimeFormat.class);
			
			return generator.invoke("writeStringField")
				.arg(JExpr.lit(fieldName)).arg(timeFormat.staticInvoke("dateTime").invoke("print").arg(fieldValue.invoke("getTimeInMillis")));
		}

		@Override
		public JStatement writeType(JCodeModel model, JVar generator, JVar value) {
		

			JClass timeFormat = model.ref(ISODateTimeFormat.class);
			
			return generator.invoke("writeString")
				.arg(timeFormat.staticInvoke("dateTime").invoke("print").arg(value.invoke("getTimeInMillis")));
		}
	}
	
	static class DateType extends JacksonType {

		@Override
		public JStatement writeTypeField(JCodeModel model, JVar generator, String fieldName, JVar fieldValue) {
			
			JClass timeFormat = model.ref(ISODateTimeFormat.class);
			
			return generator.invoke("writeStringField")
				.arg(JExpr.lit(fieldName)).arg(timeFormat.staticInvoke("date").invoke("print").arg(fieldValue.invoke("getTimeInMillis")));
		}

		@Override
		public JStatement writeType(JCodeModel model, JVar generator, JVar value) {
		

			JClass timeFormat = model.ref(ISODateTimeFormat.class);
			
			return generator.invoke("writeString")
				.arg(timeFormat.staticInvoke("date").invoke("print").arg(value.invoke("getTimeInMillis")));
		}
		
	}
	
	static class StringType extends JacksonType {

		@Override
		public JStatement writeTypeField(JCodeModel model, JVar generator, String fieldName, JVar fieldValue) {
			return generator.invoke("writeStringField").arg(JExpr.lit(fieldName)).arg(fieldValue);
		}

		@Override
		public JStatement writeType(JCodeModel model, JVar generator, JVar value) {
			return generator.invoke("writeString").arg(value);
		}
		
	}
	
	static class NumberType extends JacksonType {

		@Override
		public JStatement writeTypeField(JCodeModel model, JVar generator, String fieldName, JVar fieldValue) {
			return generator.invoke("writeNumberField").arg(JExpr.lit(fieldName)).arg(fieldValue);
			
		}

		@Override
		public JStatement writeType(JCodeModel model, JVar generator, JVar value) {
			return generator.invoke("writeNumber").arg(value);
			
		}
		
	}
	
	
}

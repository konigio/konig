package io.konig.shacl.json;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

public class XmlSchemaToJsonDatatypeMapperTest {
	
	private XmlSchemaToJsonDatatypeMapper mapper = new XmlSchemaToJsonDatatypeMapper();

	@Ignore
	public void test() {

		verify(XMLSchema.STRING, JsonDatatype.STRING);
		verify(XMLSchema.BOOLEAN, JsonDatatype.BOOLEAN);
		verify(XMLSchema.DECIMAL, JsonDatatype.NUMBER);
		verify(XMLSchema.FLOAT, JsonDatatype.NUMBER);
		verify(XMLSchema.DOUBLE, JsonDatatype.NUMBER);
		verify(XMLSchema.DURATION, JsonDatatype.STRING);
		verify(XMLSchema.DATETIME, JsonDatatype.STRING);
		verify(XMLSchema.DATE, JsonDatatype.STRING);
		verify(XMLSchema.GYEARMONTH, JsonDatatype.STRING);
		verify(XMLSchema.GYEAR, JsonDatatype.STRING);
		verify(XMLSchema.GMONTHDAY, JsonDatatype.STRING);
		verify(XMLSchema.GDAY, JsonDatatype.STRING);
		verify(XMLSchema.HEXBINARY, JsonDatatype.STRING);
		verify(XMLSchema.BASE64BINARY, JsonDatatype.STRING);
		verify(XMLSchema.ANYURI, JsonDatatype.STRING);
		verify(XMLSchema.QNAME, JsonDatatype.STRING);
		verify(XMLSchema.NOTATION, JsonDatatype.STRING);
		
		verify(XMLSchema.NORMALIZEDSTRING, JsonDatatype.STRING);
		verify(XMLSchema.TOKEN, JsonDatatype.STRING);
		verify(XMLSchema.LANGUAGE, JsonDatatype.STRING);
		verify(XMLSchema.NMTOKEN, JsonDatatype.STRING);
		verify(XMLSchema.NMTOKENS, JsonDatatype.STRING);
		verify(XMLSchema.NAME, JsonDatatype.STRING);
		verify(XMLSchema.NCNAME, JsonDatatype.STRING);
		verify(XMLSchema.ID, JsonDatatype.STRING);
		verify(XMLSchema.IDREF, JsonDatatype.STRING);
		verify(XMLSchema.INTEGER, JsonDatatype.NUMBER);
		verify(XMLSchema.NON_POSITIVE_INTEGER, JsonDatatype.NUMBER);
		verify(XMLSchema.NEGATIVE_INTEGER, JsonDatatype.NUMBER);
		verify(XMLSchema.LONG, JsonDatatype.NUMBER);
		verify(XMLSchema.INT, JsonDatatype.NUMBER);
		verify(XMLSchema.SHORT, JsonDatatype.NUMBER);
		verify(XMLSchema.BYTE, JsonDatatype.NUMBER);
		verify(XMLSchema.NON_NEGATIVE_INTEGER, JsonDatatype.NUMBER);
		verify(XMLSchema.UNSIGNED_BYTE, JsonDatatype.NUMBER);
		verify(XMLSchema.UNSIGNED_INT, JsonDatatype.NUMBER);
		verify(XMLSchema.UNSIGNED_LONG, JsonDatatype.NUMBER);
		verify(XMLSchema.UNSIGNED_SHORT, JsonDatatype.NUMBER);
		verify(XMLSchema.POSITIVE_INTEGER, JsonDatatype.NUMBER);
		
	}

	private void verify(URI rdfDatatype, JsonDatatype expected) {
		JsonDatatype actual = mapper.jsonDatatype(rdfDatatype);
		assertEquals(expected, actual);
		
	}

}

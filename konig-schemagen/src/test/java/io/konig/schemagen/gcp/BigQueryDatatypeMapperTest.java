package io.konig.schemagen.gcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.shacl.PropertyConstraint;

public class BigQueryDatatypeMapperTest {

	BigQueryDatatypeMapper map = new BigQueryDatatypeMapper();
	PropertyConstraint constraint = new PropertyConstraint();
	
	@Test
	public void test() {

		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.BYTE));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.INT));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.INTEGER));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.LONG));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.NEGATIVE_INTEGER));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.NON_NEGATIVE_INTEGER));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.NON_POSITIVE_INTEGER));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.SHORT));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.UNSIGNED_BYTE));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.UNSIGNED_INT));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.UNSIGNED_LONG));
		assertEquals(BigQueryDatatype.INTEGER, type(XMLSchema.UNSIGNED_SHORT));
		
		assertEquals(BigQueryDatatype.FLOAT, type(XMLSchema.FLOAT));
		assertEquals(BigQueryDatatype.FLOAT, type(XMLSchema.DECIMAL));
		assertEquals(BigQueryDatatype.FLOAT, type(XMLSchema.DOUBLE));
		
	}

	private BigQueryDatatype type(URI datatype) {
		constraint.setDatatype(datatype);
		return map.type(constraint);
	}

}

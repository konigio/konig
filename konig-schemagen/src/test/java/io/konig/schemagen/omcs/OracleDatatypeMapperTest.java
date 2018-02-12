package io.konig.schemagen.omcs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.schemagen.sql.FacetedSqlDatatype;
import io.konig.schemagen.sql.NumericSqlDatatype;
import io.konig.schemagen.sql.OracleDatatypeMapper;
import io.konig.schemagen.sql.SqlDatatype;
import io.konig.schemagen.sql.StringSqlDatatype;
import io.konig.shacl.PropertyConstraint;

public class OracleDatatypeMapperTest {
	
	OracleDatatypeMapper map = new OracleDatatypeMapper();
	PropertyConstraint constraint = new PropertyConstraint();
	
	@Test
	public void test() {
		
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 3, 0) , type(XMLSchema.BYTE));
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 3, 0) , type(XMLSchema.UNSIGNED_BYTE));
		
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 5, 0) , type(XMLSchema.SHORT));
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 5, 0) , type(XMLSchema.UNSIGNED_SHORT));
		
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 10, 0), type(XMLSchema.INT));
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 10, 0), type(XMLSchema.UNSIGNED_INT));
		
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 19, 0), type(XMLSchema.NEGATIVE_INTEGER));
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 19, 0), type(XMLSchema.NON_NEGATIVE_INTEGER));
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 19, 0), type(XMLSchema.INTEGER));
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 19, 0), type(XMLSchema.LONG));
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 19, 0), type(XMLSchema.UNSIGNED_LONG));
	
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.FLOAT, 126), type(XMLSchema.FLOAT));
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.FLOAT, 126), type(XMLSchema.DOUBLE));
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.FLOAT, 63), type(XMLSchema.DECIMAL));
		
		assertEqualsTest(new StringSqlDatatype(SqlDatatype.VARCHAR2, 4000), type(XMLSchema.STRING));
		assertEqualsTest(SqlDatatype.DATE, type(XMLSchema.DATE));
		assertEqualsTest(SqlDatatype.TIMESTAMP, type(XMLSchema.DATETIME));
		
		constraint.setMinInclusive(10D);
		constraint.setMaxInclusive(20D);
		assertEqualsTest(new NumericSqlDatatype(SqlDatatype.NUMBER, 2, 0) , type(XMLSchema.INT));
	}

	private FacetedSqlDatatype type(URI datatype) {
		constraint.setDatatype(datatype);
		return map.type(constraint);
	}
	
	private void assertEqualsTest(Object expected, Object actual) {
		assertEquals(expected.toString(), actual.toString());
	}
}

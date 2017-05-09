package io.konig.transform.sql.query;

import io.konig.sql.query.SignedNumericLiteral;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.ValueExpression;

public class SqlUtil {

	public static ValueExpression literal(Object value) {
		if (value instanceof String) {
			return new StringLiteralExpression(value.toString());
		}
		if (value instanceof Number) {
			Number number = (Number) value;
			return new SignedNumericLiteral(number);
		}
		
		return null;
	}
}

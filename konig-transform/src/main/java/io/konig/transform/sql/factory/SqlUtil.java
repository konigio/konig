package io.konig.transform.sql.factory;

import org.openrdf.model.URI;

import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.SignedNumericLiteral;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
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

	public static ValueExpression columnExpression(TableItemExpression table, URI predicate) {
		String columnName = columnName(table, predicate);
		return new ColumnExpression(columnName);
	}

	public static String columnName(TableItemExpression table, URI predicate) {
		String name = predicate.getLocalName();
		if (table instanceof TableAliasExpression) {
			TableAliasExpression tableName = (TableAliasExpression) table;
			StringBuilder builder = new StringBuilder();
			builder.append(tableName.getAlias());
			builder.append('.');
			builder.append(name);
			name = builder.toString();
		}
		return name;
	}
}

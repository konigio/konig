package io.konig.sql.query;

import io.konig.core.io.PrettyPrintable;

public interface QueryExpression extends PrettyPrintable{
	

	public void dispatch(QueryExpressionVisitor visitor);
}

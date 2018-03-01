package io.konig.sql.query;

public interface QueryExpressionVisitor {

	void enter(QueryExpression subject);
	void visit(QueryExpression subject, String predicate, QueryExpression object);
	void leave(QueryExpression subject);
}

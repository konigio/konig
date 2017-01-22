package io.konig.sql.query;

public class AliasExpression extends AbstractExpression 
implements ValueExpression {
	private QueryExpression expression;
	private String alias;

	
	public AliasExpression(QueryExpression expression, String alias) {
		this.expression = expression;
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}


	public QueryExpression getExpression() {
		return expression;
	}



	@Override
	public void append(StringBuilder builder) {
		expression.append(builder);
		builder.append(" AS ");
		builder.append(alias);
	}

	@Override
	public String getTargetName() {
		return alias;
	}


}

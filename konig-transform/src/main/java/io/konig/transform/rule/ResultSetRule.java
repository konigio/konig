package io.konig.transform.rule;

public class ResultSetRule {
	
	private ResultSet resultSet;
	private BooleanExpression joinCondition;
	
	public ResultSetRule(ResultSet resultSet, BooleanExpression joinCondition) {
		this.resultSet = resultSet;
		this.joinCondition = joinCondition;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public BooleanExpression getJoinCondition() {
		return joinCondition;
	}
	
	

}

package io.konig.transform.proto;

import io.konig.transform.rule.ResultSet;

public class ProtoResultSetRule {
	
	private ResultSet ruleSet;
	private ProtoBooleanExpression joinCondition;
	
	public ProtoResultSetRule(ResultSet ruleSet, ProtoBooleanExpression joinCondition) {
		this.ruleSet = ruleSet;
		this.joinCondition = joinCondition;
	}

	public ResultSet getResultSet() {
		return ruleSet;
	}

	public ProtoBooleanExpression getJoinCondition() {
		return joinCondition;
	}

	
}

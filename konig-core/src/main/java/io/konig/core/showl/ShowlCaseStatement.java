package io.konig.core.showl;

import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

public class ShowlCaseStatement implements ShowlExpression {

	private ShowlExpression caseCondition;
	private List<ShowlWhenThenClause> whenThenList;
	private ShowlExpression elseClause;
	

	public ShowlCaseStatement(ShowlExpression caseCondition, List<ShowlWhenThenClause> whenThenList,
			ShowlExpression elseClause) {
		this.caseCondition = caseCondition;
		this.whenThenList = whenThenList;
		this.elseClause = elseClause;
	}

	public ShowlExpression getCaseCondition() {
		return caseCondition;
	}

	public List<ShowlWhenThenClause> getWhenThenList() {
		return whenThenList;
	}

	public ShowlExpression getElseClause() {
		return elseClause;
	}

	@Override
	public String displayValue() {
		StringBuilder builder = new StringBuilder();
		builder.append("CASE ");
		
		for (ShowlWhenThenClause whenThen : whenThenList) {
			builder.append(whenThen);
			builder.append(' ');
		}
		builder.append("END");
		
		return builder.toString();
	}
	

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		
		if (caseCondition!=null) {
			caseCondition.addDeclaredProperties(sourceNodeShape, set);
		}
		for (ShowlWhenThenClause whenThen : whenThenList) {
			whenThen.addDeclaredProperties(sourceNodeShape, set);
		}
		if (elseClause!=null) {
			elseClause.addDeclaredProperties(sourceNodeShape, set);
		}

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		
		if (caseCondition!=null) {
			caseCondition.addProperties(set);
		}
		for (ShowlWhenThenClause whenThen : whenThenList) {
			whenThen.addProperties(set);
		}
		if (elseClause!=null) {
			elseClause.addProperties(set);
		}

	}

	@Override
	public String toString() {
		return displayValue();
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		URI result = null;
		for (ShowlWhenThenClause c : whenThenList) {
			URI type = c.getThen().valueType(reasoner);
			result = (URI) reasoner.leastCommonSuperClass(result, type);
		}
		return result;
	}
}

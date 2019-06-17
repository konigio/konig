package io.konig.core.showl;

import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.formula.Expression;

public class ShowlWhenThenClause implements ShowlExpression {

	private ShowlExpression when;
	private ShowlExpression then;

	public ShowlWhenThenClause(ShowlExpression when, ShowlExpression then) {
		this.when = when;
		this.then = then;
	}

	@Override
	public String displayValue() {
		StringBuilder builder = new StringBuilder();
		builder.append("WHEN ");
		builder.append(when.displayValue());
		builder.append(" THEN ");
		builder.append(then.displayValue());
		return builder.toString();
		
	}

	public ShowlExpression getWhen() {
		return when;
	}

	public ShowlExpression getThen() {
		return then;
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		
		when.addDeclaredProperties(sourceNodeShape, set);
		then.addDeclaredProperties(sourceNodeShape, set);

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		when.addProperties(set);
		then.addProperties(set);
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return then.valueType(reasoner);
	}

}

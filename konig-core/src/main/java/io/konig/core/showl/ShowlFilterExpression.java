package io.konig.core.showl;

import java.util.Set;

public class ShowlFilterExpression implements ShowlExpression {

	private ShowlExpression value;
	

	public ShowlFilterExpression(ShowlExpression value) {
		this.value = value;
	}

	@Override
	public String displayValue() {
		StringBuilder builder = new StringBuilder();
		builder.append("Filter(");
		builder.append(value.displayValue());
		builder.append(")");
		return builder.toString();
	}
	
	public String toString() {
		return displayValue();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		value.addDeclaredProperties(sourceNodeShape, set);
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		value.addProperties(set);
	}

	public ShowlExpression getValue() {
		return value;
	}

}

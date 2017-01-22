package io.konig.sql.query;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseValueContainer extends AbstractExpression implements ValueContainer {


	private List<ValueExpression> values = new ArrayList<>();
	
	@Override
	public void add(ValueExpression value) {
		values.add(value);

	}

	@Override
	public ValueExpression findValue(String targetName) {
		for (ValueExpression v : values) {
			if (targetName.equals(v.getTargetName())) {
				return v;
			}
			if (v instanceof ValueContainer) {
				ValueContainer c = (ValueContainer)v;
				ValueExpression e = c.findValue(targetName);
				if (e != null) {
					return e;
				}
			}
		}
		return null;
	}

	@Override
	public List<ValueExpression> getValues() {
		return values;
	}


	@Override
	public ValueExpression getValue(String targetName) {
		for (ValueExpression v : values) {
			if (targetName.equals(v.getTargetName())) {
				return v;
			}
		}
		return null;
	}
}

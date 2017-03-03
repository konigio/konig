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
	public List<ValueExpression> getValues() {
		return values;
	}


}

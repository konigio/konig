package io.konig.sql.query;

import java.util.List;

public interface ValueContainer {

	void add(ValueExpression value);
	
	List<ValueExpression> getValues();
}

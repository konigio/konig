package io.konig.cadl;

import io.konig.formula.QuantifiedExpression;

public interface HasFormula {

	void setFormula(QuantifiedExpression e);
	QuantifiedExpression getFormula();
}

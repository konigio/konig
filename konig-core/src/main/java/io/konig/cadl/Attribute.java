package io.konig.cadl;

import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.CADL;
import io.konig.formula.QuantifiedExpression;

public class Attribute extends CadlEntity  implements HasFormula {
	
	private QuantifiedExpression formula;

	@Override
	public URI getType() {
		return CADL.Attribute;
	}

	@Override
	public void setFormula(QuantifiedExpression e) {
		formula = e;
	}

	@Override
	@RdfProperty(CADL.Term.formula)
	public QuantifiedExpression getFormula() {
		return formula;
	}

}

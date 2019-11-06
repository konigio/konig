package io.konig.formula;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

abstract public class BNodeExpression extends AbstractFormula {


	private List<PredicateObjectList> constraints;

	protected BNodeExpression(List<PredicateObjectList> constraints) {
		this.constraints = constraints;
	}

	public List<PredicateObjectList> getConstraints() {
		return constraints;
	}
	
	@Override
	public HasPathStep clone() {
		HasPathStep other = new HasPathStep(new ArrayList<>());
		for (PredicateObjectList pol : constraints) {
			other.getConstraints().add(pol.clone());
		}
		return other;
		
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print('[');
		String delim = "";
		for (PredicateObjectList po : constraints) {
			out.print(delim);
			po.print(out);
			delim = "; ";
		}
		out.print(']');
		
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		for (PredicateObjectList po : constraints) {
			po.dispatch(visitor);
		}
		visitor.exit(this);
		
	}



}

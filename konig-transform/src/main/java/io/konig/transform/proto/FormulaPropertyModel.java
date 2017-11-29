package io.konig.transform.proto;

import org.openrdf.model.URI;

import io.konig.formula.Formula;

public class FormulaPropertyModel extends PropertyModel {
	
	private Formula formula;

	public FormulaPropertyModel(URI predicate, PropertyGroup group, Formula formula) {
		super(predicate, group);
		this.formula = formula;
	}

	@Override
	public ShapeModel getValueModel() {
		return null;
	}

	public Formula getFormula() {
		return formula;
	}
	
	

}

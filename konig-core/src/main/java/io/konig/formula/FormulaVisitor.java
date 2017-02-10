package io.konig.formula;

public interface FormulaVisitor {

	void enter(Formula formula);
	void exit(Formula formula);
}

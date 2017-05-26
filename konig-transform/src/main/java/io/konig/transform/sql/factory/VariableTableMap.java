package io.konig.transform.sql.factory;

import io.konig.sql.query.TableItemExpression;

public interface VariableTableMap {
	
	/**
	 * Get an expression for the Table provides values for a given variable.
	 * @param varName The name of the variable
	 * @return An expression for the Table that provides values for the variable.
	 */
	TableItemExpression tableForVariable(String varName);

}

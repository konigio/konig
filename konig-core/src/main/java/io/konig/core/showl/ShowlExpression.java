package io.konig.core.showl;

import java.util.Set;

/**
 * An expression that constructs the value of a target property within the 
 * context of an ETL process.
 * 
 * @author Greg McFall
 *
 */
public interface ShowlExpression {

	public ShowlNodeShape rootNode();
	
	public String displayValue();

	/**
	 * Collect from this expression all properties declared by the specified source NodeShape, or any 
	 * NodeShape nested within the source NodeShape.
	 * @param sourceNodeShape
	 * @param set  The set to which the properties should be added.
	 */
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set);
}

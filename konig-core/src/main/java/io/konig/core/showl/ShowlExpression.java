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

	public void addRequiredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set);
}

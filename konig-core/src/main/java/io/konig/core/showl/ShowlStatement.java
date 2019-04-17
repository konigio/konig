package io.konig.core.showl;

import java.util.Set;

public interface ShowlStatement {

	/**
	 * Collect the properties reachable from a given NodeShape.
	 * @param sourceNode  The NodeShape whose properties are to be returned.
	 * @param set The set of properties declared by sourceNode, or by a NodeShape nested within sourceNode.
	 */
	void addDeclaredProperties(ShowlNodeShape sourceNode, Set<ShowlPropertyShape> set);
}

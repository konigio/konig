package io.konig.core.showl;

/**
 * A condition that describes the join between two source shapes.
 * @author Greg McFall
 *
 */
public class ShowlSourceToSourceJoinCondition extends ShowlDerivedJoinCondition {

	public ShowlSourceToSourceJoinCondition(ShowlJoinCondition derivedFrom, ShowlPropertyShape left, ShowlPropertyShape right,
			ShowlJoinCondition previous) {
		super(derivedFrom, left, right, previous);
	}

}

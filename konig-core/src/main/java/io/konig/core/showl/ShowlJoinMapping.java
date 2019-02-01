package io.konig.core.showl;

/**
 * A mapping defined within a given JoinCondition
 * @author Greg McFall
 *
 */
public class ShowlJoinMapping extends ShowlMapping {
	

	public ShowlJoinMapping(ShowlJoinCondition joinCondition) {
		super(joinCondition, joinCondition.getLeft(), joinCondition.getRight());
	}

}

package io.konig.core.showl;

public class ShowlJoinMapping extends ShowlMapping {
	

	public ShowlJoinMapping(ShowlJoinCondition joinCondition) {
		super(joinCondition, joinCondition.getLeft(), joinCondition.getRight());
	}

}

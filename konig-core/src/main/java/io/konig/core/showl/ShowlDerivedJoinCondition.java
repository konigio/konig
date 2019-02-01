package io.konig.core.showl;

public class ShowlDerivedJoinCondition extends ShowlJoinCondition {

	private ShowlJoinCondition derivedFrom;
	
	public ShowlDerivedJoinCondition(ShowlJoinCondition derivedFrom, ShowlPropertyShape left, ShowlPropertyShape right, ShowlJoinCondition previous) {
		super(left, right, previous);
		this.derivedFrom = derivedFrom;
	}

	public ShowlJoinCondition getDerivedFrom() {
		return derivedFrom;
	}
	
	

}

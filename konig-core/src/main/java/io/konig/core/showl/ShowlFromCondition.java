package io.konig.core.showl;

public class ShowlFromCondition extends ShowlDerivedJoinCondition {

	private ShowlNodeShape sourceNode;
	
	public ShowlFromCondition(ShowlJoinCondition derivedFrom, ShowlNodeShape sourceNode) {
		super(derivedFrom, null, null, null);
		this.sourceNode = sourceNode;
	}

	public ShowlNodeShape focusNode() {
		return sourceNode;
	}
	

}

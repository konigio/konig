package io.konig.core.showl;


/**
 * A condition that joins a source shape to the target shape. 
 * @author Greg McFall
 *
 */
public class ShowlTargetToSourceJoinCondition extends ShowlJoinCondition {
	
	private ShowlSourceToSourceJoinCondition sourceToSource;

	public ShowlTargetToSourceJoinCondition(ShowlPropertyShape left, ShowlPropertyShape right, ShowlJoinCondition previous) {
		super(left, right, previous);
	}

	public ShowlSourceToSourceJoinCondition getSourceToSource() {
		return sourceToSource;
	}

	public void setSourceToSource(ShowlSourceToSourceJoinCondition sourceToSource) {
		this.sourceToSource = sourceToSource;
	}
	
	

}

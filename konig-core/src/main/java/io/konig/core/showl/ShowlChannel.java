package io.konig.core.showl;

/**
 * A structure holding information about a source NodeShape and optionally a statement 
 * for joining the specified source node to some other source node.
 * @author Greg McFall
 *
 */
public class ShowlChannel {
	
	private ShowlNodeShape sourceShape;
	private ShowlStatement joinStatement;
	
	public ShowlChannel(ShowlNodeShape sourceShape, ShowlStatement joinStatement) {
		this.sourceShape = sourceShape;
		this.joinStatement = joinStatement;
	}

	public ShowlNodeShape getSourceNode() {
		return sourceShape;
	}

	public ShowlStatement getJoinStatement() {
		return joinStatement;
	}
	
	public String toString() {
		return "ShowlChannel(" + sourceShape.getPath() + ", " + 
				(joinStatement==null ? "null" : joinStatement.toString() + ")");
	}

	

}

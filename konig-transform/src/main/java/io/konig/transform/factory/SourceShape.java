package io.konig.transform.factory;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.JoinStatement;

public class SourceShape extends ShapeNode<SourceProperty> {
	
	public static SourceShape create(Shape shape) {
		return SourceShapeFactory.INSTANCE.createShapeNode(shape);
	}
	
	private DataChannel dataChannel;
	private ProtoJoinStatement joinStatement;
	
	public SourceShape(Shape shape) {
		super(shape);
	}
	
	public void setDataChannel(DataChannel channel) {
		this.dataChannel = channel;
		for (SourceProperty sp : getProperties()) {
			SourceShape child = sp.getNestedShape();
			if (child != null) {
				child.setDataChannel(channel);
			}
		}
	}
	
	public DataChannel getDataChannel() {
		return dataChannel;
	}
	
	/**
	 * Count the total number of properties from this SourceShape that
	 * are the preferred properties.
	 * @return
	 */
	public int preferredPropertyCount() {
		int count = 0;
		for (SourceProperty sp : getProperties()) {
			TargetProperty tp = sp.getMatch();
			if (tp != null && tp.getPreferredMatch() == sp && tp.isDirectProperty()) {
				count++;
			}
			if (sp.getNestedShape()!=null) {
				count += sp.getNestedShape().preferredPropertyCount();
			}
		}
		return count;
	}
	
	/**
	 * Count the number of potential matches from this SourceShape.
	 * @return
	 */
	public int potentialMatchCount() {
		int count = 0;
		for (SourceProperty sp : getProperties()) {
			TargetProperty tp = sp.getMatch();
			if (tp!=null && tp.getPreferredMatch()==null && tp.isDirectProperty()) {
				count++;
			}
			if (sp.getNestedShape()!=null) {
				count += sp.getNestedShape().potentialMatchCount();
			}
		}
		return count;
	}
	
	/**
	 * Commit to using this SourceShape.
	 * Make the SourceProperties the preferred properties.
	 */
	public void commit() {
		for (SourceProperty sp : getProperties()) {
			TargetProperty tp = sp.getMatch();
			if (tp != null && tp.getPreferredMatch()==null) {
				tp.setPreferredMatch(sp);
			}
			if (sp.getNestedShape() != null) {
				sp.getNestedShape().commit();
			}
		}
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		
		out.field("preferredPropertyCount", preferredPropertyCount());
	}

	public ProtoJoinStatement getProtoJoinStatement() {
		return joinStatement;
	}
	
	public JoinStatement getJoinStatement() {
		return joinStatement == null ? null : joinStatement.toJoinStatement();
	}

	public void setProtoJoinStatement(ProtoJoinStatement joinStatement) {
		this.joinStatement = joinStatement;
	}
	
}

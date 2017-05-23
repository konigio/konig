package io.konig.transform.factory;

import io.konig.core.KonigException;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.JoinStatement;
import io.konig.transform.rule.VariableNamer;

public class SourceShape extends ShapeNode<SourceProperty> {
	
	public static SourceShape create(Shape shape) {
		return SourceShapeFactory.INSTANCE.createShapeNode(shape);
	}
	
	private DataChannel dataChannel;
	private DataSource dataSource;
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
	
	public DataChannel produceDataChannel(VariableNamer namer) {
		if (dataChannel == null) {
			
			if (joinStatement != null) {
				joinStatement.getLeft().produceDataChannel(namer);
			}

			Shape shape = getShape();
			if (shape==null) {
				dataChannel = parentDataChannel(namer);
			} else {
				dataChannel = new DataChannel(namer.next(), shape);
			}
			
			if (joinStatement != null && dataChannel.getJoinStatement()==null) {
				SourceShape left = joinStatement.getLeft();
				DataChannel leftChannel = left.produceDataChannel(namer);
				dataChannel.setJoinStatement(new JoinStatement(leftChannel, dataChannel, joinStatement.getCondition()));
			}
			if (dataSource!=null && dataChannel.getDatasource()==null) {
				dataChannel.setDatasource(dataSource);
			}
		}
		
	
		return dataChannel;
	}
	
	private DataChannel parentDataChannel(VariableNamer namer) {
		SourceProperty accessor = getAccessor();
		if (accessor != null) {
			SourceShape parent = accessor.getParent();
			return parent.produceDataChannel(namer);
		}

		throw new KonigException("DataChannel not found");
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
	
	

	public void setProtoJoinStatement(ProtoJoinStatement joinStatement) {
		this.joinStatement = joinStatement;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	
	
}

package io.konig.transform.rule;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;

public class DataChannel extends AbstractPrettyPrintable implements Comparable<DataChannel> {

	private String name;
	private Shape shape;
	private JoinStatement joinStatement;
	
	public DataChannel(String name, Shape value) {
		this.name = name;
		this.shape = value;
	}
	

	public DataChannel(String name, Shape shape, JoinStatement joinStatement) {
		this.name = name;
		this.shape = shape;
		this.joinStatement = joinStatement;
	}


	@Override
	public int compareTo(DataChannel other) {
		return name.compareTo(other.name);
	}
	

	public String getName() {
		return name;
	}

	public Shape getShape() {
		return shape;
	}

	public JoinStatement getJoinStatement() {
		return joinStatement;
	}

	public void setJoinStatement(JoinStatement joinStatement) {
		this.joinStatement = joinStatement;
	}


	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("name", name);
		out.beginObjectField("shape", shape);
		out.field("id", shape.getId());
		out.endObjectField(shape);
		out.field("joinStatement", joinStatement);
		out.endObject();		
	}
	
	
}

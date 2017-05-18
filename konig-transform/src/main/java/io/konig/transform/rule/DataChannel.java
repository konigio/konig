package io.konig.transform.rule;

import io.konig.shacl.Shape;

public class DataChannel implements Comparable<DataChannel> {

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
	
	
}

package io.konig.transform.rule;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class DataChannel extends AbstractPrettyPrintable implements Comparable<DataChannel> {

	private String name;
	private Shape shape;
	private JoinStatement joinStatement;
	private DataSource datasource;
	private ShapeRule parent;
	private String variableName;
	
	public DataChannel(String name, Shape value) {
		this.name = name;
		this.shape = value;
	}

	public DataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(DataSource datasource) {
		this.datasource = datasource;
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


	public ShapeRule getParent() {
		return parent;
	}


	public void setParent(ShapeRule parent) {
		this.parent = parent;
	}

	/**
	 * Get the name of the variable bound to this DataChannel.
	 * @return The name of the variable bound to this DataChannel, or null if there is no such variable.
	 */
	public String getVariableName() {
		return variableName;
	}

	/**
	 * Set the name of the variable bound to this DataChannel.
	 * @param variableName The name of the variable bound to this DataChannel.
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	
	
	
}

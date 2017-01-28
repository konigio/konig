package io.konig.sql;

import java.util.List;

public class ForeignKeyConstraint extends SQLConstraint {
	private List<SQLColumnSchema> source;
	private List<SQLColumnSchema> target;

	public ForeignKeyConstraint() {
	}

	public ForeignKeyConstraint(String name) {
		super(name);
	}

	public List<SQLColumnSchema> getSource() {
		return source;
	}

	public void setSource(List<SQLColumnSchema> source) {
		this.source = source;
	}

	public List<SQLColumnSchema> getTarget() {
		return target;
	}

	public void setTarget(List<SQLColumnSchema> target) {
		this.target = target;
	}

}

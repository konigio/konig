package io.konig.sql;

public class SQLConstraint {

	private String name;

	public SQLConstraint() {
	}

	public SQLConstraint(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

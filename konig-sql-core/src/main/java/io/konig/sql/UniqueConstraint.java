package io.konig.sql;

import java.util.List;

public class UniqueConstraint extends SQLConstraint {
	
	private List<SQLColumnSchema> columnList;

	public UniqueConstraint() {
	}

	public UniqueConstraint(String name) {
		super(name);
	}

	public List<SQLColumnSchema> getColumnList() {
		return columnList;
	}

	public void setColumnList(List<SQLColumnSchema> columnList) {
		this.columnList = columnList;
	}

}

package io.konig.sql;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKeyConstraint extends SQLConstraint {
	
	private List<SQLColumnSchema> columnList = new ArrayList<>();

	public PrimaryKeyConstraint() {
	}

	public PrimaryKeyConstraint(String name) {
		super(name);
	}
	
	public void addColumn(String columnName) {
		columnList.add(new SQLColumnSchema(columnName));
	}
	
	public void addColumn(SQLColumnSchema column) {
		columnList.add(column);
	}

	public List<SQLColumnSchema> getColumnList() {
		return columnList;
	}

	public void setColumnList(List<SQLColumnSchema> columnList) {
		this.columnList = columnList;
	}
}

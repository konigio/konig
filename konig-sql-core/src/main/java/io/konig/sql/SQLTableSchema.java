package io.konig.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLTableSchema {
	
	private String tableName;
	private SQLSchema schema;
	
	private List<SQLConstraint> constraints = new ArrayList<>();
	
	private Map<String,SQLColumnSchema> columnMap = new HashMap<>();
	

	public SQLTableSchema() {
	}


	public SQLTableSchema(String tableName) {
		this.tableName = tableName;
	}
	public SQLTableSchema(SQLSchema schema, String tableName) {
		this.schema = schema;
		this.tableName = tableName;
		schema.add(this);
	}


	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	public SQLSchema getSchema() {
		return schema;
	}


	public void setSchema(SQLSchema schema) {
		this.schema = schema;
	}


	public void addColumn(SQLColumnSchema columnSchema) {
		if (columnSchema.getColumnName() == null) {
			throw new SQLSchemaException("Column name must be defined");
		}
		columnMap.put(columnSchema.getColumnName(), columnSchema);
	}
	
	public SQLColumnSchema getColumnByName(String columnName) {
		return columnMap.get(columnName);
	}
	
	public Collection<SQLColumnSchema> listColumns() {
		return columnMap.values();
	}
	
	public String getFullName() {
		StringBuilder builder = new StringBuilder();
		String schemaName = schema==null ? "global" : schema.getSchemaName();
		String localName = tableName;
		builder.append(schemaName);
		builder.append('.');
		builder.append(localName);
		
		return builder.toString();
	}


	public List<SQLConstraint> getConstraints() {
		return constraints;
	}


	public void setConstraints(List<SQLConstraint> constraints) {
		this.constraints = constraints;
	}
	
	public void addConstraint(SQLConstraint constraint) {
		constraints.add(constraint);
	}

	public SQLPrimaryKeyConstraint getPrimaryKeyConstraint() {
		for (SQLConstraint c : constraints) {
			if (c instanceof SQLPrimaryKeyConstraint) {
				return (SQLPrimaryKeyConstraint) c;
			}
		}
		for (SQLColumnSchema col : listColumns()) {
			if (col.getPrimaryKey() != null) {
				SQLPrimaryKeyConstraint constraint = new SQLPrimaryKeyConstraint();
				constraint.addColumn(col);
				return constraint;
			}
		}
		return null;
	}

}

package io.konig.transform.sql.query;

import io.konig.sql.query.ValueExpression;
import io.konig.transform.MappedProperty;
import io.konig.transform.TransformAttribute;

public class SqlAttribute  {

	private TableName sourceTable;
	private TransformAttribute attribute;
	private MappedProperty mappedProperty;
	private SqlFrame embedded;
	private ValueExpression valueExpression;

	public SqlAttribute(TableName sourceTable, TransformAttribute attribute, MappedProperty mappedProperty) {
		this.sourceTable = sourceTable;
		this.attribute = attribute;
		this.mappedProperty = mappedProperty;
	}

	public SqlAttribute(TableName sourceTable, TransformAttribute attribute, ValueExpression valueExpression) {
		this.sourceTable = sourceTable;
		this.attribute = attribute;
		this.valueExpression = valueExpression;
	}
	
	

	public TableName getSourceTable() {
		return sourceTable;
	}

	public TransformAttribute getAttribute() {
		return attribute;
	}

	public MappedProperty getMappedProperty() {
		return mappedProperty;
	}

	public SqlFrame getEmbedded() {
		return embedded;
	}

	public void setEmbedded(SqlFrame embedded) {
		this.embedded = embedded;
	}

	public ValueExpression getValueExpression() {
		return valueExpression;
	}
	
}

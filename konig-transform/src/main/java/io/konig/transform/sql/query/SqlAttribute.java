package io.konig.transform.sql.query;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.MappedProperty;
import io.konig.transform.TransformAttribute;

public class SqlAttribute extends AbstractPrettyPrintable  {

	/**
	 * The table from which this attribute is sourced.
	 */
	private TableName sourceTable;
	
	/**
	 * A description of the target attribute
	 */
	private TransformAttribute attribute;
	
	/**
	 * A description of the property from the source table that provides 
	 * the value of the target attribute
	 */
	private MappedProperty mappedProperty;
	
	/**
	 * If the target attribute is a nested record, this frame
	 * provides a description of that record.
	 */
	private SqlFrame embedded;
	
	/**
	 * An expression that represents a derived value for the
	 * target attribute.
	 */
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

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.beginObject(this);
		out.field("attribute", attribute);
		out.field("embedded", embedded);
		out.field("mappedProperty", mappedProperty);
		out.field("sourceTable", sourceTable);
		out.field("valueExpression", valueExpression);
		
		out.popIndent();
		
	}

	
}

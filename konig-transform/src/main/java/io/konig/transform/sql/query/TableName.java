package io.konig.transform.sql.query;

import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;

public class TableName extends AbstractPrettyPrintable {
	private String fullName;
	private String alias;
	private TableItemExpression item;
	
	public TableName(String fullName, String alias) {
		this.fullName = fullName;
		this.alias = alias;
	}

	public String columnName(String name) {
		if (alias == null) {
			return name;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(alias);
		builder.append('.');
		builder.append(name);
		return builder.toString();
	}


	public ColumnExpression column(PropertyConstraint sourceProperty) {
		return column(sourceProperty.getPredicate().getLocalName());
	}
	public ColumnExpression column(URI predicate) {
		return column(predicate.getLocalName());
	}
	
	public ColumnExpression column(String name) {
		return new ColumnExpression(columnName(name));
	}

	public String getFullName() {
		return fullName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public TableNameExpression getExpression() {
		return new TableNameExpression(fullName);
	}

	public TableItemExpression getItem() {
		if (item == null) {
			item = new TableNameExpression(fullName);
			if (alias != null) {
				item = new TableAliasExpression(item, alias);
			}
		}
		return item;
	}

	public void setItem(TableItemExpression item) {
		this.item = item;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("fullName: ");
		out.println(fullName);
		out.indent();
		out.print("alias: ");
		out.println(alias);
		out.indent();
		out.print("item: ");
		out.println(item);
		
	}

	
}

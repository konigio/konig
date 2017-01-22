package io.konig.sql.query;

import java.util.ArrayList;
import java.util.List;

public class FromExpression extends AbstractExpression implements QueryExpression {
	
	private List<TableItemExpression> tableItems = new ArrayList<>();
	
	
	
	public List<TableItemExpression> getTableItems() {
		return tableItems;
	}

	public void add(TableItemExpression e) {
		tableItems.add(e);
	}

	@Override
	public void append(StringBuilder builder) {
	
		builder.append("FROM ");
		String comma = "";
		for (TableItemExpression item : tableItems) {
			builder.append(comma);
			item.append(builder);
			comma = ", ";
		}
		
	}

	
}

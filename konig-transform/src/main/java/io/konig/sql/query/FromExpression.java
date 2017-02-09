package io.konig.sql.query;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.KonigException;
import io.konig.core.io.PrettyPrintWriter;

public class FromExpression extends AbstractExpression implements QueryExpression {
	
	private List<TableItemExpression> tableItems = new ArrayList<>();
	
	
	
	public List<TableItemExpression> getTableItems() {
		return tableItems;
	}

	public void add(TableItemExpression e) {
		if (e == null) {
			throw new KonigException("TableItem must not be null");
		}
		tableItems.add(e);
	}

	@Override
	public void print(PrettyPrintWriter out) {
	
		out.print("FROM ");
		String comma = "";
		for (TableItemExpression item : tableItems) {
			out.print(comma);
			item.print(out);
			comma = ", ";
		}
		
	}

	
}

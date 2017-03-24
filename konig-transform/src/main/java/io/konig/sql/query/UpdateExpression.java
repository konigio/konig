package io.konig.sql.query;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class UpdateExpression extends AbstractExpression implements DmlExpression {
	
	private TableItemExpression table;
	private List<UpdateItem> itemList = new ArrayList<>();
	private FromExpression from;
	private SearchCondition where;
	
	public TableItemExpression getTable() {
		return table;
	}

	public void setTable(TableItemExpression table) {
		this.table = table;
	}

	public FromExpression getFrom() {
		return from;
	}

	public void setFrom(FromExpression from) {
		this.from = from;
	}

	public SearchCondition getWhere() {
		return where;
	}

	public void setWhere(SearchCondition where) {
		this.where = where;
	}

	public void add(UpdateItem item) {
		itemList.add(item);
	}
	
	public List<UpdateItem> getItemList() {
		return itemList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("UPDATE ");
		table.print(out);
		out.println();
		out.indent();
		out.print("SET");
		if (itemList.size()==1) {
			out.print(' ');
			itemList.get(0).print(out);
		} else {
			out.pushIndent();
			String comma = "";
			for (UpdateItem item : itemList) {
				out.println(comma);
				comma = ",";
				out.indent();
				item.print(out);
			}
			out.popIndent();
		}
		if (from != null) {
			out.println();
			out.indent();
			from.print(out);
		}
		if (where != null) {
			out.println();
			out.indent();
			out.print("WHERE ");
			where.print(out);
		}

	}

}

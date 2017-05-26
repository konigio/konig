package io.konig.sql.query;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class GroupByClause extends AbstractExpression {
	
	private List<GroupingElement> elementList;

	public GroupByClause(List<GroupingElement> elementList) {
		this.elementList = elementList;
	}
	
	public void add(GroupingElement element) {
		if (elementList == null) {
			elementList = new ArrayList<>();
		}
		elementList.add(element);
	}

	public List<GroupingElement> getElementList() {
		return elementList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print("GROUP BY ");
		String comma = "";
		for (GroupingElement e : elementList) {
			out.print(comma);
			comma = ", ";
			out.print(e);
		}
	}

}

package io.konig.sql.query;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class AndExpression extends AbstractExpression implements BooleanTerm {
	
	private List<BooleanTerm> termList = new ArrayList<>();

	
	public void add(BooleanTerm term) {
		termList.add(term);
	}

	public List<BooleanTerm> getTermList() {
		return termList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		String and = null;
		out.pushIndent();
		for (BooleanTerm term : termList) {
			if (and != null) {
				out.println();
				out.indent();
				out.print(and);
			} 
			term.print(out);
			and = "AND ";
		}
		out.popIndent();
		
		
	}

}

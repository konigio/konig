package io.konig.sql.query;

import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class SelectExpression extends BaseValueContainer implements ValueContainer, QueryExpression, DmlExpression {
	
	
	private FromExpression from = new FromExpression();
	private WhereClause where;
	
	public FromExpression getFrom() {
		return from;
	}

	public WhereClause getWhere() {
		return where;
	}

	public void setWhere(WhereClause where) {
		this.where = where;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print("SELECT");
		List<ValueExpression> values = getValues();
		if (inline()) {
			out.print(' ');
			out.print(values.get(0));
		} else {
			out.pushIndent();
			String comma = "";
			for (ValueExpression value : values) {
				out.print(comma);
				out.println();
				out.indent();
				value.print(out);
				comma = ",";
			}
			out.popIndent();
		}
		
		out.println();
		if (from != null) {
			out.indent();
			from.print(out);
		}
		if (where != null) {
			out.println();
			out.indent();
			out.print(where);
		}
		
	}

	private boolean inline() {

		List<ValueExpression> values = getValues();
		if (values.size()==1) {
			ValueExpression e = values.get(0);
			if (e instanceof ColumnExpression) {
				return true;
			}
		}
		
		return false;
	}
	

}

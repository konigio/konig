package io.konig.sql.query;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

public class QueryWriter {
	
	private boolean prettyPrint=true;
	private PrintWriter out;
	private int indent;
	private String tab = "   ";
	
	public QueryWriter(Writer out) {
		if (out instanceof PrintWriter) {
			this.out = (PrintWriter)out;
		} else {
			this.out = new PrintWriter(out);
		}
	}
	
	public void flush() {
		out.flush();
	}
	
	public void print(BigQueryCommandLine cmd, String fileName) {

		String projectId = cmd.getProjectId();
		String destinationTable = cmd.getDestinationTable();
		boolean useLegacySql = cmd.isUseLegacySql();
		
		out.print("bq query");
		if (projectId!= null) {
			out.print(" --project_id=");
			out.print(projectId);
		}
		if (destinationTable!=null) {
			out.print(" --destination_table=");
			out.print(destinationTable);
		}
		out.print(" --use_legacy_sql=");
		out.print(useLegacySql);
		out.print(" `cat ");
		out.print(fileName);
		out.print('`');
		out.print('\n');
	}
	
	public void print(SelectExpression select) {
		printSelect(select);
		out.print(';');
	}
	
	private void printSelect(SelectExpression select) {
		out.print("SELECT");
		pushIndent();
		String comma = "";
		for (ValueExpression value : select.getValues()) {
			out.print(comma);
			println();
			indent();
			print(value);
			comma = ",";
		}
		popIndent();
		println();
		print(select.getFrom());
		
		out.flush();
	}
	
	
	private void print(FromExpression from) {
		out.print("FROM ");
		String comma = "";
		for (TableItemExpression item : from.getTableItems()) {
			out.print(comma);
			print(item);
			comma = ", ";
		}
		
	}

	private void print(TableItemExpression item) {
		if (item instanceof TableAliasExpression) {
			print((TableAliasExpression) item);
		} else if (item instanceof TableNameExpression) {
			print((TableNameExpression)item);
		} else {
			throw new RuntimeException("Unsupported TableItemExpression type: " + item.getClass().getName());
		}
		
	}
	
	private void print(TableNameExpression tableName) {
		out.print(tableName.getDatasetName());
		out.print('.');
		out.print(tableName.getTableName());
	}
	
	private void print(TableAliasExpression alias) {

		print(alias.getTableName());
		out.print(" AS ");
		out.print(alias.getAlias());
	}

	private void print(ValueExpression value) {
		if (value instanceof AliasExpression) {
			print((AliasExpression)value);
		} else if (value instanceof ColumnExpression) {
			print((ColumnExpression) value);
		} else {
			throw new RuntimeException("Unsupported ValueExpression type: " + value.getClass().getName());
		}
		
	}
	
	private void print(ColumnExpression column) {

		out.print(column.getColumnName());
	}
	
	private void print(AliasExpression alias) {
		
		print(alias.getExpression());

		out.print(" AS ");
		out.print(alias.getAlias());
	}

	private void print(QueryExpression expr) {
		if (expr instanceof FromExpression) {
			print((FromExpression)expr);
		} else if (expr instanceof SelectExpression) {
			printSelect((SelectExpression)expr);
		} else if (expr instanceof ColumnExpression) {
			print((ColumnExpression)expr);
		} else if (expr instanceof StructExpression) {
			print((StructExpression)expr);
		} else if (expr instanceof TableAliasExpression) {
			print((TableAliasExpression) expr);
		} else if (expr instanceof TableNameExpression) {
			print((TableNameExpression)expr);
		} else if (expr instanceof AliasExpression) {
			print((AliasExpression)expr);
		} else if (expr instanceof ColumnExpression) {
			print((ColumnExpression)expr);
		} else if (expr instanceof FunctionExpression) {
			print((FunctionExpression)expr);
		} else if (expr instanceof StringLiteralExpression) {
			print((StringLiteralExpression) expr);
		}
		
	}
	
	private void print(StringLiteralExpression expr) {

		out.print('"');
		out.print(expr.getValue());
		out.print('"');
	}
	
	private void print(FunctionExpression func) {

		out.print(func.getFunctionName());
		out.print('(');
		String comma = "";
		List<QueryExpression> argList = func.getArgList();
		
		for (QueryExpression e : argList) {
			out.print(comma);
			print(e);
			comma = ", ";
		}
		
		out.print(')');
	}
	
	private void print(StructExpression struct) {
		out.print("STRUCT(");
		pushIndent();
		String comma = "";
		for (QueryExpression field : struct.getValues()) {
			out.print(comma);
			if (prettyPrint || comma.length()>0) {
				println();
			}
			indent();
			print(field);
			comma = ",";
		}
		if (prettyPrint) {
			out.print('\n');
		}
		popIndent();
		indent();
		out.print(')');
	}

	private void pushIndent() {
		indent++;
	}
	
	private void popIndent() {
		indent--;
	}
	
	private void indent() {
		if (prettyPrint) {

			for (int i=0; i<indent; i++) {
				out.print(tab);
			}
		}
	}
	
	private void println() {
		if (prettyPrint) {
			out.print('\n');
		} else {
			out.print(' ');
		}
	}

}

package io.konig.sql.query;

import java.io.PrintWriter;

import io.konig.core.io.PrettyPrintWriter;

public class BigQueryCommandLine extends AbstractExpression {
	
	private String projectId;
	private String destinationTable;
	private boolean useLegacySql;
	private DmlExpression dml;
	
	

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getDestinationTable() {
		return destinationTable;
	}


	public void setDestinationTable(String destinationTable) {
		this.destinationTable = destinationTable;
	}

	public boolean isUseLegacySql() {
		return useLegacySql;
	}

	public void setUseLegacySql(boolean useLegacySql) {
		this.useLegacySql = useLegacySql;
	}

	public DmlExpression getDml() {
		return dml;
	}

	public void setDml(DmlExpression dml) {
		this.dml = dml;
	}

	public void print(PrintWriter out, String fileName) {
		
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
	
	@Override
	public void print(PrettyPrintWriter out) {
		boolean pretty = out.isPrettyPrint();
		out.setPrettyPrint(false);
		
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
		out.print('"');
		dml.print(out);
		out.print('"');
		out.println();
		
		out.setPrettyPrint(pretty);
	}
	
	

	
}

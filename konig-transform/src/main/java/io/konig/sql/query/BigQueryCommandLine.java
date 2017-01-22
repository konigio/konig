package io.konig.sql.query;

public class BigQueryCommandLine extends AbstractExpression {
	
	private String projectId;
	private String destinationTable;
	private boolean useLegacySql;
	private SelectExpression select;
	
	

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

	public SelectExpression getSelect() {
		return select;
	}

	public void setSelect(SelectExpression select) {
		this.select = select;
	}

	@Override
	public void append(StringBuilder builder) {
		
		builder.append("bq query");
		if (projectId!= null) {
			builder.append(" --project_id=");
			builder.append(projectId);
		}
		if (destinationTable!=null) {
			builder.append(" --destination_table=");
			builder.append(destinationTable);
		}
		builder.append(" --use_legacy_sql=");
		builder.append(useLegacySql);
		builder.append(' ');
		builder.append('"');
		select.append(builder);
		builder.append('"');
		
	}

	
}

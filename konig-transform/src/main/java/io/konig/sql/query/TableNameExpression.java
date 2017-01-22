package io.konig.sql.query;

public class TableNameExpression extends AbstractExpression implements TableItemExpression {
	
	private String datasetName;
	private String tableName;
	public TableNameExpression(String datasetName, String tableName) {
		this.datasetName = datasetName;
		this.tableName = tableName;
	}
	@Override
	public void append(StringBuilder builder) {
		builder.append(datasetName);
		builder.append('.');
		builder.append(tableName);
		
	}
	public String getDatasetName() {
		return datasetName;
	}
	public String getTableName() {
		return tableName;
	}
	
	

}

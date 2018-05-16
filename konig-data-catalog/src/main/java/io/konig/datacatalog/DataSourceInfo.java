package io.konig.datacatalog;

public class DataSourceInfo implements Comparable<DataSourceInfo> {
	
	private String type;
	private String tableName;
	private String providedBy;
	
	public DataSourceInfo(String type, String tableName, String providedBy) {
		this.type = type;
		this.tableName = tableName;
		this.providedBy = providedBy;
	}
	
	public String getType() {
		return type;
	}
	public String getTableName() {
		return tableName;
	}
	public String getProvidedBy() {
		return providedBy;
	}

	@Override
	public int compareTo(DataSourceInfo o) {
		int result = type.compareTo(o.type);
		if (result == 0) {
			result = tableName.compareTo(o.tableName);
		}
		if (result == 0) {
			result = providedBy.compareTo(o.providedBy);
		}
		return result;
	}
	
	
	

}

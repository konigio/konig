package io.konig.sql.query;

public class SelectExpression extends BaseValueContainer implements ValueContainer, QueryExpression {
	
	
	private FromExpression from = new FromExpression();
	
	public FromExpression getFrom() {
		return from;
	}
	
	public void addFrom(String datasetName, String tableName) {
		TableNameExpression e = new TableNameExpression(datasetName, tableName);
		from.add(e);
	}
	
	public TableItemExpression fromTable(String datasetName, String tableName) {
		
		for (TableItemExpression e : from.getTableItems() ) {
			if (e instanceof TableAliasExpression) {
				TableAliasExpression alias = (TableAliasExpression) e;
				if (matches(alias.getTableName(), datasetName, tableName)) {
					return alias;
				}
			} else if (e instanceof TableNameExpression) {
				TableNameExpression t = (TableNameExpression) e;
				if (matches(t, datasetName, tableName)) {
					return t;
				}
			}
		}
		
		return null;
	}

	private boolean matches(TableNameExpression e, String datasetName, String tableName) {
		
		return tableName.equals(e.getTableName()) && datasetName.equals(e.getDatasetName());
	}

	@Override
	public void append(StringBuilder builder) {
		
		builder.append("SELECT ");
		String comma = "";
		for (ValueExpression value : getValues()) {
			builder.append(comma);
			value.append(builder);
			comma = ", ";
		}
		builder.append(' ');
		from.append(builder);
		
	}
	

}

package io.konig.omcs.datasource;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OMCS;
import io.konig.datasource.BaseTableDataSource;

public class OracleTable extends BaseTableDataSource {
	private String tableName;
	private OracleTableReference tableReference;
	private String tableId;
	
	public OracleTable() {
		addType(Konig.OracleTable);
	}
	
	public void setTableId(String tableId){
		this.tableId = tableId;
	}
	
	public String getTableId(){
		return tableId;
	}
	
	@RdfProperty(OMCS.TABLE_REFERENCE)
	public OracleTableReference getTableReference() {
		return tableReference;
	}
	
	public void setTableReference(OracleTableReference tableReference) {
		this.tableReference = tableReference;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableIdentifier() {
		return tableName;
	}

	@Override
	public void setId(Resource id) {
		super.setId(id);
		if (tableName == null && id instanceof URI) {
			URI uri = (URI) id;
			tableName = uri.getLocalName();
		}
	}

	@Override
	public String getUniqueIdentifier() {
		StringBuilder builder = new StringBuilder();
		builder.append("OracleTable:");
		builder.append(tableReference.getOmcsInstanceId());
		builder.append(':');
		builder.append(tableReference.getOracleSchema());
		builder.append(':');
		builder.append(tableReference.getOmcsTableId());
		return builder.toString();
	}

	@Override
	public String getSqlDialect() {
		// TODO: Need to supply the version number supported.
		return "PL/SQL";
	}

	@Override
	public String getDdlFileName() {
		OracleTableReference ref = getTableReference();
		StringBuilder builder = new StringBuilder();
		builder.append(ref.getOmcsInstanceId());
		builder.append('.');
		builder.append(ref.getOracleSchema());
		builder.append('.');
		builder.append(ref.getOmcsTableId());
		builder.append(".sql");
		return builder.toString();
	}

}

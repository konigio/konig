package io.konig.gcp.deployment;

import com.google.api.services.bigquery.model.Table;

public class BigqueryTableResource extends BaseGcpResource<Table> {

	public BigqueryTableResource() {
		super("bigquery.v2.table");
	}

}

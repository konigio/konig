package io.konig.gcp.deployment;

public class BigqueryDatasetResource extends BaseGcpResource<BigqueryDatasetProperties> {

	public BigqueryDatasetResource() {
		super("bigquery.v2.dataset");
	}

}

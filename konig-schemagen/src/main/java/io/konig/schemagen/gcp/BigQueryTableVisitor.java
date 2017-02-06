package io.konig.schemagen.gcp;

import com.google.api.services.bigquery.model.Table;

public interface BigQueryTableVisitor {
	
	public void visit(Table table);

}

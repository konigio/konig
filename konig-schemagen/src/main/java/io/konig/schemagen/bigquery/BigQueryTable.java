package io.konig.schemagen.bigquery;

import org.openrdf.model.URI;

public class BigQueryTable {

	private BigQueryTableReference tableReference;
	private String description;
	private URI tableShape;

	public BigQueryTableReference getTableReference() {
		return tableReference;
	}

	public void setTableReference(BigQueryTableReference tableReference) {
		this.tableReference = tableReference;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public URI getTableShape() {
		return tableShape;
	}

	public void setTableShape(URI tableShape) {
		this.tableShape = tableShape;
	}
	
	
	
	
}

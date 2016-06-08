package io.konig.schemagen.gcp;

import org.openrdf.model.URI;

public class BigQueryTable {

	private String tableId;
	private BigQueryDataset tableDataset;
	private String description;
	private URI tableShape;
	private URI tableClass;

	public BigQueryTableReference getTableReference() {
		
		if (tableId == null) {
			throw new GoogleCloudException("tableId is not defined");
		}
		if (tableDataset == null) {
			throw new GoogleCloudException("tableDataset is not defined for table " + tableId);
		}
		String datasetId = tableDataset.getDatasetId();
		if (datasetId == null) {
			throw new GoogleCloudException("datasetId is not defined for table " + tableId);
		}
		GoogleCloudProject project = tableDataset.getDatasetProject();
		if (project == null) {
			throw new GoogleCloudException("project is not defined for dataset " + datasetId);
		}
		String projectId = project.getProjectId();
		if (projectId == null) {
			throw new GoogleCloudException("projectId is not defined for dataset " + datasetId);
		}
		
		
		return new BigQueryTableReference(projectId, datasetId, tableId);
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the Shape of data recorded in the table.
	 * Either tableShape or tableClass must be defined.
	 * @return The Shape of data recorded in the table.
	 */
	public URI getTableShape() {
		return tableShape;
	}

	public void setTableShape(URI tableShape) {
		this.tableShape = tableShape;
	}

	/**
	 * Get the OWL class for instances recorded in the table.
	 * Either the tableClass or tableShape property must be defined.
	 * @return The OWL class for instances recorded in the table.
	 */
	public URI getTableClass() {
		return tableClass;
	}

	public void setTableClass(URI tableClass) {
		this.tableClass = tableClass;
	}

	public BigQueryDataset getTableDataset() {
		return tableDataset;
	}

	public void setTableDataset(BigQueryDataset ownerDataset) {
		this.tableDataset = ownerDataset;
	}


	public String getTableId() {
		return tableId;
	}


	public void setTableId(String tableId) {
		this.tableId = tableId;
	}


	
	
	
	
}

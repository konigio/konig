package io.konig.spreadsheet;

import org.openrdf.model.URI;

public class SheetColumn {
	private String name;
	private boolean required;
	private int index;
	private URI iri;
	private URI datatype;
	private URI objectType;
	
	public SheetColumn(String name) {
		this.name = name;
	}
	

	public SheetColumn(String name, boolean required) {
		this.name = name;
		this.required = required;
	}


	public String getName() {
		return name;
	}
	
	public boolean exists() {
		return index>=0;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isRequired() {
		return required;
	}
	
	public String toString() {
		return "SheetColumn(" + name + ")";
	}

	/**
	 * Get the IRI name of this column
	 */
	public URI getIri() {
		return iri;
	}


	public void setIri(URI iri) {
		this.iri = iri;
	}


	/**
	 * Get the datatype for values in this column.
	 * @return The datatype for values in this column.  Null if the datatype is not
	 * known or values in the column are rdf:Resource instances.
	 */
	public URI getDatatype() {
		return datatype;
	}


	public void setDatatype(URI datatype) {
		this.datatype = datatype;
	}


	public URI getObjectType() {
		return objectType;
	}


	public void setObjectType(URI objectType) {
		this.objectType = objectType;
	}
	
	public URI getValueType() {
		return datatype==null ? objectType : datatype;
	}
	
	
	
	
}

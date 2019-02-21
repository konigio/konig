package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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

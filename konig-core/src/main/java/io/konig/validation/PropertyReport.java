package io.konig.validation;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

public class PropertyReport implements Comparable<PropertyReport>{
	private URI propertyId;
	private boolean nameHasWrongCase;
	private URI invalidXmlSchemaDatatype;
	
	public PropertyReport(URI propertyId) {
		this.propertyId = propertyId;
	}

	public boolean getNameHasWrongCase() {
		return nameHasWrongCase;
	}

	public void setNameHasWrongCase(boolean nameHasWrongCase) {
		this.nameHasWrongCase = nameHasWrongCase;
	}

	public URI getPropertyId() {
		return propertyId;
	}

	@Override
	public int compareTo(PropertyReport o) {
		return propertyId.stringValue().compareTo(o.getPropertyId().stringValue());
	}

	public URI getInvalidXmlSchemaDatatype() {
		return invalidXmlSchemaDatatype;
	}

	public void setInvalidXmlSchemaDatatype(URI invalidXmlSchemaDatatype) {
		this.invalidXmlSchemaDatatype = invalidXmlSchemaDatatype;
	}

}

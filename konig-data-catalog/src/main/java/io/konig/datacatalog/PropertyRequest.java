package io.konig.datacatalog;

import io.konig.shacl.PropertyStructure;

public class PropertyRequest extends PageRequest {

	private PropertyStructure propertyStructure;
	
	public PropertyRequest(PageRequest other) {
		super(other);
	}

	public PropertyStructure getPropertyStructure() {
		return propertyStructure;
	}

	public void setPropertyStructure(PropertyStructure propertyStructure) {
		this.propertyStructure = propertyStructure;
	}
	
}

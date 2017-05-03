package io.konig.datacatalog;

import io.konig.shacl.PropertyStructure;

public class PropertyRequest extends PageRequest {

	private PropertyStructure propertyStructure;
	
	public PropertyRequest(PageRequest other, PropertyStructure structure) {
		super(other);
		propertyStructure = structure;
	}

	public PropertyStructure getPropertyStructure() {
		return propertyStructure;
	}
	
}

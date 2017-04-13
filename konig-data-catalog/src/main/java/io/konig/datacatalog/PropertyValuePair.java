package io.konig.datacatalog;

public class PropertyValuePair {
	private Link propertyName;
	private String value;
	
	public PropertyValuePair(Link propertyName, String value) {
		this.propertyName = propertyName;
		this.value = value;
	}
	public Link getPropertyName() {
		return propertyName;
	}
	public String getValue() {
		return value;
	}
	
	

}

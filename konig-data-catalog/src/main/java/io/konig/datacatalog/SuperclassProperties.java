package io.konig.datacatalog;

import java.util.List;

public class SuperclassProperties {

	private Link superclass;
	private List<PropertyInfo> propertyList;
	
	public SuperclassProperties(Link superclass, List<PropertyInfo> propertyList) {
		this.superclass = superclass;
		this.propertyList = propertyList;
	}

	public Link getSuperclass() {
		return superclass;
	}

	public List<PropertyInfo> getPropertyList() {
		return propertyList;
	}
	
	
	
	
}

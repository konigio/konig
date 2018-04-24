package io.konig.transform.proto;

public class PropertyMatch {

	private PropertyModel sourceProperty;
	private DirectPropertyModel targetProperty;
	
	public PropertyMatch(PropertyModel sourceProperty, DirectPropertyModel targetProperty) {
		this.sourceProperty = sourceProperty;
		this.targetProperty = targetProperty;
	}
	
	public PropertyModel getSourceProperty() {
		return sourceProperty;
	}
	
	public DirectPropertyModel getTargetProperty() {
		return targetProperty;
	}
	
	
}

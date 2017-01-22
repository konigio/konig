package io.konig.transform;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.shacl.PropertyConstraint;

public class IriTemplateElement {
	
	private String text;
	private Namespace namespace;
	private PropertyConstraint property;
	
	public IriTemplateElement(String text) {
		this.text = text;
	}

	public IriTemplateElement(String text, Namespace namespace) {
		this.text = text;
		this.namespace = namespace;
	}


	public IriTemplateElement(String text, PropertyConstraint property) {
		this.text = text;
		this.property = property;
	}

	public String getText() {
		return text;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	public PropertyConstraint getProperty() {
		return property;
	}
	
	public void setProperty(PropertyConstraint p) {
		property = p;
	}

}

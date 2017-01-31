package io.konig.transform;

import org.openrdf.model.Namespace;

import io.konig.shacl.PropertyConstraint;

/**
 * A structure that provides information about one element of an {@link io.konig.core.util.IriTemplate IriTemplate}
 * <p>
 * An IriTemplateElement has the following properties:
 * <ul>
 *   <li> text: The string content of the template element. 
 *   <li> namespace:  The namespace value matching the template element.  
 *            Null if the template element does not match a namespace prefix.
 *   <li> property: The property constraint named by the template element.
 * </ul>
 * </p>
 * @author Greg McFall
 *
 */
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

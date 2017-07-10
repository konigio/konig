package io.konig.transform;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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

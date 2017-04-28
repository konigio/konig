package io.konig.shacl.jsonld;

/*
 * #%L
 * Konig Core
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
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.util.ValueFormat;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.util.ValueFormat.ElementType;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class TemplateContextNamer implements ContextNamer {
	private NamespaceManager nsManager;
	private ShapeManager shapeManager;
	private ValueFormat template;
	
	public TemplateContextNamer(NamespaceManager nsManager, ShapeManager shapeManager, ValueFormat template) {
		this.nsManager = nsManager;
		this.shapeManager = shapeManager;
		this.template = template;
	}

	@Override
	public URI forShape(URI shapeId) {
		
		StringBuilder builder = new StringBuilder();
		
		for (Element e : template.toList()) {
			String text = e.getText();
			if (e.getType() == ElementType.TEXT) {
				builder.append(text);
			} else {
				URI targetClass = null;
				switch (text) {
				case "shapeId" :
					builder.append(shapeId.stringValue());
					break;
					
				case "shapeLocalName" :
					builder.append(shapeId.getLocalName());
					break;
					
				case "shapeNamespacePrefix" :
					Namespace shapeNamespace = namespace(shapeId);
					builder.append(shapeNamespace.getPrefix());
					break;
					
				case "targetClassId" :
					builder.append(targetClass(shapeId).stringValue());
					break;
					
				case "targetClassLocalName" : 
					targetClass = targetClass(shapeId);
					builder.append(targetClass.getLocalName());
					break;
					
				case "targetClassNamespacePrefix" :
					targetClass = targetClass(shapeId);
					Namespace classNamespace = namespace(targetClass);
					builder.append(classNamespace.getPrefix());
					break;
					
				default :
					throw new KonigException("Unsupported variable: " + text);
				}
			}
		}
		
		return new URIImpl(builder.toString());
	}

	private Namespace namespace(URI uri) {
		Namespace ns = nsManager.findByName(uri.getNamespace());
		if (ns == null) {
			throw new KonigException("Namespace not found: " + uri.getNamespace());
		}
		return ns;
	}

	private URI targetClass(URI shapeId) {
		Shape shape = shapeManager.getShapeById(shapeId);
		if (shape == null) {
			throw new KonigException("Shape not found: " + shapeId);
		}
		URI targetClass = shape.getTargetClass();
		if (targetClass == null) {
			throw new KonigException("Target class not defined on Shape " + shapeId);
		}
		return targetClass;
	}
	

}

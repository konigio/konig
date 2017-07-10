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


import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Context;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.util.ValueFormatVisitor;
import io.konig.core.vocab.Konig;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class IriTemplateInfo extends ArrayList<IriTemplateElement> {
	private static final long serialVersionUID = 1L;
	
	private IriTemplate template;

	public IriTemplateInfo(IriTemplate template) {
		this.template = template;
	}
	
	
	public IriTemplate getTemplate() {
		return template;
	}


	/** 
	 * TODO: Don't pass a NamespaceManager. It is not necessary since an IriTemplate now has a Context.
	 * @param template
	 * @param nsManager
	 * @param shape
	 * @return
	 * @throws ShapeTransformException
	 */
	public static IriTemplateInfo create(IriTemplate template, NamespaceManager nsManager, Shape shape) throws ShapeTransformException {
		
		Context context = template.getContext();
		if (context == null) {
			throw new ShapeTransformException("Context must be defined for IriTemplate of " + shape.getId());
		}
		context.compile();

		IriTemplateInfo info = new IriTemplateInfo(template);
		List<? extends Element> elements = template.toList();
		URI idFormat = shape.getIdFormat();
		if (Konig.Curie.equals(idFormat)) {
			// For now, we only support Curie format if the template has the format {nsPrefix}{propertyName}
			
			if (elements.size()==2) {
				// Confirm that the first element is a namespace prefix.
				Element namespace = elements.get(0);
				Namespace ns = nsManager.findByPrefix(namespace.getText());
				if (ns == null) {
					
					String nsIri = context.expandIRI(namespace.getText());
					if (!nsIri.endsWith("/") && !nsIri.endsWith("#") && !nsIri.endsWith(":")) {
						throw new ShapeTransformException("Expected iriTemplate of the form {nsPrefix}{propertyName}");
					}
				}
				String propertyName = elements.get(1).getText();
				String predicateId = context.expandIRI(propertyName);
				if (predicateId.indexOf(':') < 0) {
					throw new ShapeTransformException("Failed to expand propertyName to a fully-qualified IRI: " + propertyName);
				}
				URI predicate = new URIImpl(predicateId);

				PropertyConstraint p = shape.getPropertyConstraint(predicate);
				if (p == null) {
					throw new ShapeTransformException("Property not found: " + propertyName);
				}
				
				info.add(new IriTemplateElement(namespace.getText() + ':'));
				info.add(new IriTemplateElement(propertyName, p));
				
			} else {
				throw new ShapeTransformException("Expected iriTemplate of the form {nsPrefix}{propertyName}");
			}
			
		} else {

			for (Element e : elements) {
				switch (e.getType()) {
				
				case TEXT:
					info.add(new IriTemplateElement(e.getText()));
					break;
					
				case VARIABLE:
					String varName = e.getText();
					String iriValue = context.expandIRI(varName);
					
					if (iriValue.equals(varName)) {
						info.add(new IriTemplateElement(e.getText()));
					} else {
						int colon = iriValue.indexOf(':');
						if (colon<0) {
							throw new ShapeTransformException("Invalid IRI: " + iriValue);
						}
						URI predicate = new URIImpl(iriValue);
						PropertyConstraint p = shape.getPropertyConstraint(predicate);
						if (p == null) {
							
							Namespace ns = nsManager.findByPrefix(varName);
							if (ns == null) {
								ns = new NamespaceImpl(varName, iriValue);
							}
							info.add(new IriTemplateElement(varName, ns));
							
						} else {
							info.add(new IriTemplateElement(e.getText(), p));
						}
					}
					
					
					break;
				}
			}
		}
		
		
		
		return info;
	}
	
	
	private static class Visitor implements ValueFormatVisitor {
		
		private NamespaceManager nsManager;
		private Shape shape;
		private IriTemplateInfo info;
		
		

		public Visitor(IriTemplate template, NamespaceManager nsManager, Shape shape) {
			this.info = new IriTemplateInfo(template);
			this.nsManager = nsManager;
			this.shape = shape;
		}

		@Override
		public void visitText(String text) {
			info.add(new IriTemplateElement(text));
		}

		@Override
		public void visitVariable(String varName) {
			
			if (info!=null) {

				IriTemplateElement e = null;
				Namespace ns = nsManager.findByPrefix(varName);
				if (ns != null) {
					e = new IriTemplateElement(varName, ns);
				} else {
					
					int colon = varName.indexOf(':');
					if (colon > 0) {
						// Interpret varName as a CURIE
						
						URI predicate = RdfUtil.expand(nsManager, varName);
						if (predicate != null) {
							PropertyConstraint p = shape.getPropertyConstraint(predicate);
							if (p != null) {
								e = new IriTemplateElement(varName, p);
							}
						}
					} else {
						// Interpret varName as a local name
						
						for (PropertyConstraint p : shape.getProperty()) {
							URI predicate = p.getPredicate();
							if (predicate!=null && varName.equals(predicate.getLocalName())) {
								e = new IriTemplateElement(varName, p);
							}
						}
					}
				}
				if (e == null) {
					info = null;
				} else {
					info.add(e);
				}
			}
			
			
		}
		
	}
	
}

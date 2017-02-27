package io.konig.transform;

import java.util.ArrayList;

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


	public static IriTemplateInfo create(IriTemplate template, NamespaceManager nsManager, Shape shape) throws ShapeTransformException {
		
		Context context = template.getContext();
		if (context == null) {
			throw new ShapeTransformException("Context must be defined for IriTemplate of " + shape.getId());
		}
		context.compile();
		
		IriTemplateInfo info = new IriTemplateInfo(template);
		for (Element e : template.toList()) {
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

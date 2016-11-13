package io.konig.schemagen.merge;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;
import io.konig.schemagen.SchemaGeneratorException;

public class SimpleShapeNamer implements ShapeNamer {
	private String baseURL;
	private NamespaceManager nsManager;

	public SimpleShapeNamer(NamespaceManager nsManager, String baseURL) {
		this.nsManager = nsManager;
		this.baseURL = baseURL;
	}

	@Override
	public URI shapeName(URI targetClass) throws SchemaGeneratorException {
		String namespace = targetClass.getNamespace();
		Namespace ns = nsManager.findByName(namespace);
		if (ns == null) {
			throw new SchemaGeneratorException("Prefix not found for namespace " + namespace);
		}
		StringBuilder builder = new StringBuilder(baseURL);
		builder.append(ns.getPrefix());
		builder.append('/');
		builder.append(targetClass.getLocalName());
		
		return new URIImpl(builder.toString());
	}
	
	

}

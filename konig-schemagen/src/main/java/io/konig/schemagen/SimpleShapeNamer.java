package io.konig.schemagen;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NamespaceManager;

public class SimpleShapeNamer implements ShapeNamer {
	private String baseURL;
	private NamespaceManager nsManager;
	private String prefixBase;

	public SimpleShapeNamer(NamespaceManager nsManager, String baseURL) {
		this.nsManager = nsManager;
		this.baseURL = baseURL;
	}

	
	public String getPrefixBase() {
		return prefixBase;
	}

	public void setPrefixBase(String prefixBase) {
		this.prefixBase = prefixBase;
	}



	@Override
	public URI shapeName(URI scopeClass) throws SchemaGeneratorException {
		String namespace = scopeClass.getNamespace();
		Namespace ns = nsManager.findByName(namespace);
		if (ns == null) {
			throw new SchemaGeneratorException("Prefix not found for namespace " + namespace);
		}
		StringBuilder builder = new StringBuilder(baseURL);
		builder.append(ns.getPrefix());
		builder.append('/');
		
		if (prefixBase != null) {
			StringBuilder buffer = new StringBuilder();
			buffer.append(prefixBase);
			buffer.append(ns.getPrefix());
			String prefix = buffer.toString();
			Namespace target = nsManager.findByPrefix(prefix);
			if (target == null) {
				nsManager.add(prefix, builder.toString());
			}
		}

		builder.append(scopeClass.getLocalName());
		
		return new URIImpl(builder.toString());
	}
	
	

}

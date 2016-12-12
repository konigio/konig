package io.konig.gae.datastore;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;

public class SimpleDaoNamer implements DaoNamer {

	
	private String basePackage;
	private NamespaceManager nsManager;	
	
	
	public SimpleDaoNamer(String basePackage, NamespaceManager nsManager) {
		this.basePackage = basePackage;
		this.nsManager = nsManager;
	}




	@Override
	public String daoName(URI owlClass) {
		Namespace ns = nsManager.findByName(owlClass.getNamespace());
		if (ns == null) {
			throw new KonigException("Namespace prefix not found: " + owlClass.getNamespace());
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(basePackage);
		builder.append('.');
		builder.append(ns.getPrefix());
		builder.append('.');
		builder.append(owlClass.getLocalName());
		builder.append("Dao");
		
		return builder.toString();
	}

}

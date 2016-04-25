package io.konig.schemagen.java;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.NamespaceManager;

public class BasicJavaNamer implements JavaNamer {
	private static final Logger logger = LoggerFactory.getLogger(BasicJavaNamer.class);
	private String basePackage;
	private String defaultPrefix = "default";
	private NamespaceManager nsManager;
	private String writerPackage;
	

	public BasicJavaNamer(String basePackage, NamespaceManager nsManager) {
		this(basePackage, null, nsManager);
	}

	public BasicJavaNamer(String basePackage, String writerPackage, NamespaceManager nsManager) {
		if (!basePackage.endsWith(".")) {
			basePackage = basePackage + ".";
		}
		if (writerPackage == null) {
			writerPackage = basePackage + "writer.";
		}
		if (!writerPackage.endsWith(".")) {
			writerPackage = writerPackage + ".";
		}
		this.basePackage = basePackage;
		this.nsManager = nsManager;
		this.writerPackage = writerPackage;
	}

	@Override
	public String javaClassName(URI owlClass) {
		String namespaceName = owlClass.getNamespace();
		Namespace ns = nsManager.findByName(namespaceName);
		if (ns == null) {
			
			if (namespaceName.equals(OWL.NAMESPACE)) {
				ns = new NamespaceImpl("owl", namespaceName);
			} else {
				logger.warn("Prefix for namespace not found: " + owlClass.getNamespace());
			}
		}
		String prefix = ns==null ? defaultPrefix : ns.getPrefix();
		
		StringBuilder builder = new StringBuilder(basePackage);
		builder.append(prefix);
		builder.append('.');
		builder.append(owlClass.getLocalName());
		
		return builder.toString();
	}

	@Override
	public String writerName(String mediaType) {
		if (mediaType == null || mediaType.length()==0) {
			return writerPackage.substring(0, writerPackage.length()-1);
		}
		
		int slash = mediaType.indexOf('/');
		if (slash>0) {
			mediaType = mediaType.substring(slash+1);
		}
		mediaType = mediaType.replace('+', '.');
		
		return writerPackage + mediaType;
	}

}

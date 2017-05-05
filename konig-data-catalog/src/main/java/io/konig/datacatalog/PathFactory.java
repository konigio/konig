package io.konig.datacatalog;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Schema;

public class PathFactory {
	private OwlReasoner reasoner;
	private NamespaceManager nsManager;
	
	public PathFactory(OwlReasoner reasoner, NamespaceManager nsManager) {
		this.reasoner = reasoner;
		this.nsManager = nsManager;
	}
	
	public String relativePath(URI a, URI b) throws DataCatalogException {
		Path aPath = Paths.get(pagePath(a));
		Path bPath = Paths.get(pagePath(b));
		Path relative = aPath.relativize(bPath);
		String result = relative.toString().replace('\\', '/');
		return result;
	}
	
	public String pagePath(URI target) throws DataCatalogException {
		String uri = target.stringValue();
		int mark = uri.length();
		while (mark > 0) {
			mark = uri.lastIndexOf('/', mark-1);
			if (mark > 0) {
				String namespace = uri.substring(0, mark+1);
				Namespace ns = nsManager.findByName(namespace);
				if (ns != null) {
					String localName = uri.substring(mark+1);
					String prefix = ns.getPrefix();

					StringBuilder builder = new StringBuilder();
					builder.append(prefix);
					builder.append('/');
					addFolder(builder, target);
					builder.append(localName);
					builder.append(".html");
					
					return builder.toString();
				}
			}
		}
		throw new DataCatalogException("Namespace not found for resource: <" + uri + ">");
	}
	
	private String folderName(URI target) {
		if (reasoner.isTypeOf(target, Schema.Enumeration)) {
			return "individuals";
		} else if (reasoner.isTypeOf(target, OWL.CLASS)) {
			return "classes";
		} else if (reasoner.isProperty(target)) {
			return "properties";
		}
		return null;
	}

	private void addFolder(StringBuilder builder, URI target) {
		String folderName = folderName(target);
		if (folderName != null) {
			builder.append(folderName);
			builder.append('/');
		}
		
	}

}

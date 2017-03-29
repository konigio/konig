package io.konig.datacatalog;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.NamespaceManager;

public class DataCatalogUtil {

	public static final String CLASSES_INDEX_FILE = "class-index.html";
	
	public static String classIndexFileName(Namespace ns) {
		StringBuilder builder = new StringBuilder();
		builder.append(ns);
		builder.append("/class-index.html");
		return builder.toString();
	}
	
	public static String classFileName(PageRequest request, URI classId) throws DataCatalogException {
		Namespace ns = request.findNamespaceByName(classId.getNamespace());
		StringBuilder builder = new StringBuilder();
		builder.append(ns.getPrefix());
		builder.append('/');
		builder.append(classId.getLocalName());
		builder.append(".html");
		return builder.toString();
	}
	
	public static String relativePath(PageRequest request, URI a, URI b) throws DataCatalogException {
		if (a==null || b==null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		Namespace na = request.findNamespaceByName(a.getNamespace());
		Namespace nb = request.findNamespaceByName(b.getNamespace());
		if (!na.getName().equals(nb.getName())) {
			builder.append("../");
			builder.append(nb.getPrefix());
			builder.append('/');
		} 
		builder.append(b.getLocalName());
		builder.append(".html");
		
		return builder.toString();
	}
}

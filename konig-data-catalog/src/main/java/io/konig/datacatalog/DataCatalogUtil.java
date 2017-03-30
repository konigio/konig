package io.konig.datacatalog;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class DataCatalogUtil {

	public static final String CLASSES_INDEX_FILE = "class-index.html";
	private static final String ONTOLOGY_SUMMARY = "ontology-summary";
	public static final String ONTOLOGY_INDEX_FILE = "ontology-index.html";
	
	public static String classIndexFileName(Namespace ns) {
		StringBuilder builder = new StringBuilder();
		builder.append(ns);
		builder.append("/class-index.html");
		return builder.toString();
	}
	
	public static URI ontologySummary(String namespace) {
		return new URIImpl(namespace + ONTOLOGY_SUMMARY);
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
	
	/**
	 * The path to a resource relative to the base directory.
	 */
	public static String path(PageRequest request, URI resourceId) throws DataCatalogException {
		StringBuilder builder = new StringBuilder();
		Namespace ns = request.findNamespaceByName(resourceId.getNamespace());
		builder.append(ns.getPrefix());
		builder.append('/');
		builder.append(resourceId.getLocalName());
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

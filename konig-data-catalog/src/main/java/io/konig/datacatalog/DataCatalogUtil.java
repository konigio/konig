package io.konig.datacatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.VANN;

public class DataCatalogUtil {

	public static final String CLASSES_INDEX_FILE = "class-index.html";
	private static final String ONTOLOGY_SUMMARY = "ontology-summary";
	public static final String ONTOLOGY_INDEX_FILE = "ontology-index.html";
	public static final String SITE_NAME = "SiteName";
	
	public static String classIndexFileName(Namespace ns) {
		StringBuilder builder = new StringBuilder();
		builder.append(ns);
		builder.append("/class-index.html");
		return builder.toString();
	}
	
	public static void setSiteName(PageRequest request) {
	
		DataCatalogBuildRequest buildRequest = request.getBuildRequest();
		String siteName = buildRequest.getSiteName();
		if (siteName == null) {
			URI ontologyId = buildRequest.getOntologyId();
			if (ontologyId != null) {

				Vertex targetOntology = buildRequest.getGraph().getVertex(ontologyId);
				if (targetOntology != null) {
					siteName = ontologyName(targetOntology);
				} else {
					siteName = "Data Catalog";
				}
			}
			buildRequest.setSiteName(siteName);
			
		}
		request.getContext().put(SITE_NAME, siteName);
	}
	
	public static List<Vertex> ontologyList(PageRequest request) throws DataCatalogException {
		
		DataCatalogBuildRequest buildRequest = request.getBuildRequest();
		
		List<Vertex> result = buildRequest.getOntologyList();
		if (result == null) {
			Set<URI> ontologySet = buildRequest.getOntologyInclude();
			Graph graph = buildRequest.getGraph();
			if (ontologySet != null) {
				result = new ArrayList<>();
				for (URI id : ontologySet) {
					Vertex v = graph.getVertex(id);
					if (v == null) {
						throw new DataCatalogException("Ontology not found: " + id);
					}
					result.add(v);
				}
			} else {
				URI ontologyId = buildRequest.getOntologyId();
				Vertex v = graph.getVertex(ontologyId);
				if (v==null) {
					throw new DataCatalogException("Ontology not found: " + ontologyId);
				}
				result = v.asTraversal().out(OWL.IMPORTS).toVertexList();
				result.add(v);
			}
			buildRequest.setOntologyList(result);
		}
		
		return result;
	}
	
	public static String ontologyName(Vertex ontology) {
		Value value = ontology.getValue(RDFS.LABEL);
		if (value == null) {
			value = ontology.getValue(VANN.preferredNamespacePrefix);
		}
		
		return value == null ? ontology.getId().stringValue() : value.stringValue();
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
}

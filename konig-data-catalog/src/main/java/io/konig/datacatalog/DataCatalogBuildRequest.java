package io.konig.datacatalog;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.SH;
import io.konig.shacl.ShapeManager;

public class DataCatalogBuildRequest {
	private String siteName;
	private URI ontologyId;
	private File outDir;
	private File exampleDir;
	private Graph graph; 
	private ShapeManager shapeManager;
	private Set<URI> ontologyInclude;
	private Set<URI> ontologyExclude;
	private List<Vertex> ontologyList;
	
	
	public DataCatalogBuildRequest() {
		
	}

	public URI getOntologyId() {
		return ontologyId;
	}

	public void setOntologyId(URI ontologyId) {
		this.ontologyId = ontologyId;
	}

	public File getOutDir() {
		return outDir;
	}

	public void setOutDir(File outDir) {
		this.outDir = outDir;
	}

	public File getExampleDir() {
		return exampleDir;
	}

	public void setExampleDir(File exampleDir) {
		this.exampleDir = exampleDir;
	}

	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}

	public void setShapeManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public Set<URI> getOntologyInclude() {
		return ontologyInclude;
	}

	public void setOntologyInclude(Set<URI> ontologySet) {
		this.ontologyInclude = ontologySet;
	}

	public List<Vertex> getOntologyList() {
		return ontologyList;
	}

	public void setOntologyList(List<Vertex> ontologyList) {
		this.ontologyList = ontologyList;
	}

	public Set<URI> getOntologyExclude() {
		return ontologyExclude;
	}

	public void setOntologyExclude(Set<URI> ontologyExclude) {
		this.ontologyExclude = ontologyExclude;
	}
	
	public void useDefaultOntologyList() throws DataCatalogException {
		if (graph == null) {
			throw new DataCatalogException("graph must be defined");
		}
		
		Set<URI> exclude = ontologyExclude();
		List<Vertex> list = graph.v(OWL.ONTOLOGY).in(RDF.TYPE).toVertexList();
		Iterator<Vertex> sequence = list.iterator();
		while (sequence.hasNext()) {
			Vertex v = sequence.next();
			if (exclude.contains(v.getId())) {
				sequence.remove();
			}
		}
		ontologyList = list;
	}

	private Set<URI> ontologyExclude() {
		if (ontologyExclude == null) {
			Set<URI> set = new HashSet<>();
			add(set, RDF.NAMESPACE);
			add(set, RDFS.NAMESPACE);
			add(set, OWL.NAMESPACE);
			add(set, SH.NAMESPACE);
			return set;
		}
		return ontologyExclude;
	}

	private void add(Set<URI> set, String value) {
		set.add(uri(value));
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	
}

package io.konig.datacatalog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.ShapeManager;

public class PageRequest {
	private static final String OWL_CLASS_LIST = "OwlClassList";

	private Vertex targetOntology;
	private VelocityEngine engine;
	private VelocityContext context;
	private Graph graph;
	private ClassStructure classStructure;
	private ShapeManager shapeManager;
	private DataCatalogBuilder builder;
	private URI pageId;
	private Set<URI> indexSet;

	public PageRequest(
		DataCatalogBuilder builder,
		Vertex targetOntology, 
		VelocityEngine engine, 
		Graph graph, 
		ClassStructure classStructure,
		ShapeManager shapeManager
	) {
		this.builder = builder;
		this.targetOntology = targetOntology;
		this.engine = engine;
		this.graph = graph;
		this.classStructure = classStructure;
		this.shapeManager = shapeManager;
		indexSet = new HashSet<>();
	}
	
	public PageRequest(PageRequest other) {
		this.builder = other.builder;
		this.targetOntology = other.targetOntology;
		this.engine = other.getEngine();
		this.context = new VelocityContext();
		this.graph = other.getGraph();
		this.classStructure = other.getClassStructure();
		this.shapeManager = other.getShapeManager();
		this.indexSet = other.getIndexSet();
	}

	public Set<URI> getIndexSet() {
		return indexSet;
	}

	public DataCatalogBuilder getBuilder() {
		return builder;
	}

	public ClassStructure getClassStructure() {
		return classStructure;
	}

	public URI getPageId() {
		return pageId;
	}

	public void setPageId(URI pageId) {
		this.pageId = pageId;
	
		if (indexSet!=null && !pageId.stringValue().startsWith(DataCatalogBuilder.CATALOG_BASE_URI)) {
			String localName = pageId.getLocalName();
			if (localName.length()>0) {
				indexSet.add(pageId);
			}
		}
	}
	
	public void setActiveLink(URI targetId) throws DataCatalogException {
		builder.setActiveItem(this, targetId);
	}

	public Object put(String key, Object value) {
		return context.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> type) {
		return (T) context.get(key);
	}

	public VelocityEngine getEngine() {
		return engine;
	}

	public void setContext(VelocityContext context) {
		this.context = context;
	}

	public VelocityContext getContext() {
		if (context == null) {
			context = new VelocityContext();
		}
		return context;
	}

	public Graph getGraph() {
		return graph;
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}
	
	public Vertex getTargetOntology() {
		return targetOntology;
	}

	public Namespace findNamespaceByName(String name) throws DataCatalogException {
		NamespaceManager nsManager = getNamespaceManager();
		Namespace ns = nsManager.findByName(name);
		if (ns == null) {
			throw new DataCatalogException("Namespace not found: " + name);
		}
		return ns;
	}

	public NamespaceManager getNamespaceManager() throws DataCatalogException {

		NamespaceManager nsManager = graph.getNamespaceManager();
		if (nsManager == null) {
			throw new DataCatalogException("NamespaceManager is not defined");
		}
		return nsManager;
	}
	
	public List<Vertex> getOwlClassList() {
		@SuppressWarnings("unchecked")
		List<Vertex> list = (List<Vertex>) context.get(OWL_CLASS_LIST);
		if (list == null) {
			OwlReasoner reasoner = new OwlReasoner(graph);
			list = reasoner.owlClassList();
			context.put(OWL_CLASS_LIST, list);
		}
		
		return list;
	}
	
	public String relativePath(URI a, URI b) throws DataCatalogException {
		return builder.relativePath(this, a, b);
	}
	
	public String relativePath(URI target) throws DataCatalogException {
		return builder.relativePath(this, pageId, target);
	}
}

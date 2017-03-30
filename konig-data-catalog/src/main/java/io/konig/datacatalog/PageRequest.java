package io.konig.datacatalog;

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Namespace;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.shacl.ShapeManager;

public class PageRequest {
	private static final String OWL_CLASS_LIST = "OwlClassList";

	private VelocityEngine engine;
	private VelocityContext context;
	private Graph graph;
	private ShapeManager shapeManager;

	public PageRequest(VelocityEngine engine, VelocityContext context, Graph graph, ShapeManager shapeManager) {
		this.engine = engine;
		this.context = context;
		this.graph = graph;
		this.shapeManager = shapeManager;
	}
	
	public PageRequest(PageRequest other) {
		this.engine = other.getEngine();
		this.context = new VelocityContext();
		this.graph = other.getGraph();
		this.shapeManager = other.getShapeManager();
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

	public VelocityContext getContext() {
		return context;
	}

	public Graph getGraph() {
		return graph;
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
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
}

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

	private VelocityEngine engine;
	private VelocityContext context;
	private ClassStructure classStructure;
	private DataCatalogBuildRequest buildRequest;
	private DataCatalogBuilder builder;
	private URI pageId;
	private Set<URI> indexSet;

	public PageRequest(
		DataCatalogBuildRequest buildRequest,
		DataCatalogBuilder builder,
		VelocityEngine engine, 
		ClassStructure classStructure
	) {
		this.buildRequest = buildRequest;
		this.builder = builder;
		this.engine = engine;
		this.classStructure = classStructure;
		indexSet = new HashSet<>();
	}
	
	public PageRequest(PageRequest other) {
		this.buildRequest = other.buildRequest;
		this.builder = other.builder;
		this.engine = other.getEngine();
		this.context = new VelocityContext();
		this.classStructure = other.getClassStructure();
		this.indexSet = other.getIndexSet();
	}

	public DataCatalogBuildRequest getBuildRequest() {
		return buildRequest;
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
		return buildRequest.getGraph();
	}

	public ShapeManager getShapeManager() {
		return buildRequest.getShapeManager();
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

		NamespaceManager nsManager = getGraph().getNamespaceManager();
		if (nsManager == null) {
			throw new DataCatalogException("NamespaceManager is not defined");
		}
		return nsManager;
	}
	
	public List<Vertex> getOwlClassList() {
		@SuppressWarnings("unchecked")
		List<Vertex> list = (List<Vertex>) context.get(OWL_CLASS_LIST);
		if (list == null) {
			OwlReasoner reasoner = new OwlReasoner(getGraph());
			list = reasoner.owlClassList();
			context.put(OWL_CLASS_LIST, list);
		}
		
		return list;
	}
	
	public String relativePath(URI a, URI b) throws DataCatalogException {
		if (a==null || b==null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		Namespace na = findNamespaceByName(a.getNamespace());
		Namespace nb = findNamespaceByName(b.getNamespace());
		String aFolder = folderName(a);
		String bFolder = folderName(b);
		String aNamespace = na.getName();
		String bNamespace = nb.getName();
		
		if (!aNamespace.equals(bNamespace)) {
			
			if (!aNamespace.equals(DataCatalogBuilder.CATALOG_BASE_URI)) {
				builder.append("../");
				if (aFolder != null) {
					builder.append("../");
				}
			} 
			if (!bNamespace.equals(DataCatalogBuilder.CATALOG_BASE_URI)) {
				builder.append(nb.getPrefix());
				builder.append('/');
				if (bFolder != null) {
					builder.append(bFolder);
					builder.append('/');
				}
			}
		} else {
			if (!equals(aFolder, bFolder)) {
				if (aFolder != null) {
					builder.append("../");
				}
				if (bFolder != null) {
					builder.append(bFolder);
					builder.append('/');
				}
			}
		}
		builder.append(b.getLocalName());
		builder.append(".html");
		
		return builder.toString();
	}
	
	private boolean equals(String a, String b) {
		
		return (a==null && b==null) ||
			(a!=null && a.equals(b));
	}

	public String folderName(URI entity) {
		if (classStructure.shapeForClass(entity) != null) {
			return "classes";
		}
		if (classStructure.getProperty(entity) != null) {
			return "properties";
		}
		return null;
	}

	public String relativePath(URI target) throws DataCatalogException {
		return relativePath(pageId, target);
	}
}

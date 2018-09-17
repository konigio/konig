package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.ShapeManager;

public class PageRequest {
	private static final String OWL_CLASS_LIST = "OwlClassList";

	private VelocityContext context;
	private DataCatalogBuildRequest buildRequest;
	private URI pageId;
	private Set<URI> indexSet;

	public PageRequest(
		DataCatalogBuildRequest buildRequest
	) {
		this.buildRequest = buildRequest;
		indexSet = new HashSet<>();
	}
	
	public PageRequest(PageRequest other) {
		this.buildRequest = other.buildRequest;
		this.context = new VelocityContext();
		this.indexSet = other.getIndexSet();
	}

	public DataCatalogBuildRequest getBuildRequest() {
		return buildRequest;
	}

	public Set<URI> getIndexSet() {
		return indexSet;
	}

	public DataCatalogBuilder getBuilder() {
		return buildRequest.getCatalogBuilder();
	}
	
	public OwlReasoner getOwlReasoner() {
		return getClassStructure().getReasoner();
	}

	public ClassStructure getClassStructure() {
		return buildRequest.getClassStructure();
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
		getBuilder().setActiveItem(this, targetId);
	}

	public Object put(String key, Object value) {
		return context.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> type) {
		return (T) context.get(key);
	}

	public VelocityEngine getEngine() {
		return buildRequest.getEngine();
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
	
	/**
	 * Get the relative path between two resources.
	 * @param a Resource from which the path will be computed.
	 * @param b Resource to which the path will be computed.
	 * @return The relative path from a to b.
	 * @throws DataCatalogException
	 */
	public String relativePath(URI a, URI b) throws DataCatalogException {
		
		return buildRequest.getPathFactory().relativePath(a, b);
	}
	
	
	public void handleTermStatus(Resource subject) throws DataCatalogException {
		Vertex v = buildRequest.getGraph().getVertex(subject);
		if (v != null) {
			Value termStatus = v.getValue(Konig.termStatus);
			if (termStatus instanceof URI) {
				putTermStatus((URI) termStatus);
			}
		}
	}
	
	public Link termStatusLink(URI termStatus) throws DataCatalogException {

		if (termStatus != null) {			
			String name = stringValue(termStatus, Schema.name, RDFS.LABEL);
			if (name == null) {
				name = termStatus.getLocalName();
			}
			String href = relativePath(termStatus);
			return new Link(name, href);
		}
		return null;
	}

	public void putTermStatus(URI termStatus) throws DataCatalogException {
		
		Link link = termStatusLink(termStatus);
		if (link != null) {
			context.put("termStatus", link);
		}
	}

	private String stringValue(URI subject, URI...predicate) {
		Graph graph = buildRequest.getGraph();
		Vertex v = graph.getVertex(subject);
		if (v != null) {
			for (URI p : predicate) {
				Value value = v.getValue(p);
				if (value != null) {
					return value.stringValue();
				}
			}
		}
				
		return null;
	}

	public String relativePath(URI target) throws DataCatalogException {
		return relativePath(pageId, target);
	}
}

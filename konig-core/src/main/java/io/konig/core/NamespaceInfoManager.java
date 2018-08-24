package io.konig.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class NamespaceInfoManager {
	
	private Map<String, NamespaceInfo> map = new HashMap<>();
	
	public void load(Graph graph) {
		for (Vertex v : graph.vertices()) {
			Resource node = v.getId();
			if (node instanceof URI) {
				URI iri = (URI) node;
				String namespace = iri.getNamespace();
				
				NamespaceInfo set = map.get(namespace);
				if (set == null) {
					set = new NamespaceInfo(namespace);
					map.put(namespace, set);
				}
				set.getTerms().add(iri);
			}
		}
	}
	
	public Collection<NamespaceInfo> listNamespaces() {
		return map.values();
	}
	
	public NamespaceInfo getNamespaceInfo(String namespace) {
		return map.get(namespace);
	}

}

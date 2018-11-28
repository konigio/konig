package io.konig.shacl.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

public class GraphOwlClass {
	
	private URI owlClassId;
	
	private Map<URI, GraphPropertyGroup> outbound = new HashMap<>();
	private Map<URI, GraphPropertyGroup> inbound = new HashMap<>();
	private Set<GraphOwlClass> superClasses = null;
	
	public GraphOwlClass(URI owlClassId) {
		this.owlClassId = owlClassId;
	}

	public URI getOwlClassId() {
		return owlClassId;
	}

	public GraphPropertyGroup produceOutboundGroup(URI predicate) {
		GraphPropertyGroup g = outbound.get(predicate);
		if (g == null) {
			g = new GraphPropertyGroup();
			outbound.put(predicate, g);
		}
		return g;
	}
	
	public void addSuperClass(GraphOwlClass superclass) {
		if (superClasses == null) {
			superClasses = new HashSet<>();
		}
		superClasses.add(superclass);
	}
	
	public Set<GraphOwlClass> getSuperClasses() {
		return superClasses;
	}

	public GraphPropertyGroup produceInboundGroup(URI predicate) {
		GraphPropertyGroup g = inbound.get(predicate);
		if (g == null) {
			g = new GraphPropertyGroup();
			inbound.put(predicate, g);
		}
		return g;
	}

	public String toString() {
		return "GraphOwlClass[" + owlClassId.getLocalName() + "]";
	}
}

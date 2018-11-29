package io.konig.core.showl;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

public class ShowlClass {
	
	private URI owlClassId;
	
	private Set<ShowlProperty> domainOf = new HashSet<>();
	private Set<ShowlProperty> rangeOf = new HashSet<>();
	private Set<ShowlClass> superClasses = null;
	
	public ShowlClass(URI owlClassId) {
		this.owlClassId = owlClassId;
	}

	public URI getId() {
		return owlClassId;
	}

	
	public void addSuperClass(ShowlClass superclass) {
		if (superClasses == null) {
			superClasses = new HashSet<>();
		}
		superClasses.add(superclass);
	}
	
	public Set<ShowlClass> getSuperClasses() {
		return superClasses;
	}

	public String toString() {
		return "GraphOwlClass[" + owlClassId.getLocalName() + "]";
	}
	
	public void addRangeOf(ShowlProperty p) {
		rangeOf.add(p);
	}
	
	public void addDomainOf(ShowlProperty p) {
		domainOf.add(p);
	}
}

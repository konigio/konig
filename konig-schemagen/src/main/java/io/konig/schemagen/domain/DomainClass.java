package io.konig.schemagen.domain;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class DomainClass {
	
	private Vertex classVertex;
	private Set<URI> superClass = new HashSet<>();

	public DomainClass(Vertex classVertex) {
		this.classVertex = classVertex;
	}

	public Vertex getClassVertex() {
		return classVertex;
	}

	public Set<URI> getSuperClass() {
		return superClass;
	}
	
	public void addSuperClass(URI superClass) {
		this.superClass.add(superClass);
	}
}

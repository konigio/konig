package io.konig.core.impl;

import org.openrdf.model.impl.URIImpl;

import io.konig.core.Vertex;

public class URIVertex extends URIImpl implements ResourceVertex {
	private static final long serialVersionUID = 1L;
	private Vertex vertex;
	
	public URIVertex(String iriValue, Vertex vertex) {
		super(iriValue);
		this.vertex = vertex;
	}
	
	public Vertex getVertex() {
		return vertex;
	}

	public VertexImpl getVertexImpl() {
		return (VertexImpl) vertex;
	}

}

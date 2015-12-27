package io.konig.core.impl;

import org.openrdf.model.impl.BNodeImpl;

import io.konig.core.Vertex;

public class BNodeVertex extends BNodeImpl implements ResourceVertex{
	private static final long serialVersionUID = 1L;
	
	private Vertex vertex;
	public BNodeVertex(String nodeId, Vertex vertex) {
		super(nodeId);
		this.vertex = vertex;
	}
	
	public Vertex getVertex() {
		return vertex;
	}

	public VertexImpl getVertexImpl() {
		return (VertexImpl) vertex;
	}

}

package io.konig.core.impl;

import org.openrdf.model.Resource;

import io.konig.core.Vertex;

public interface ResourceVertex extends Resource {

	Vertex getVertex();
	
	VertexImpl getVertexImpl();
}

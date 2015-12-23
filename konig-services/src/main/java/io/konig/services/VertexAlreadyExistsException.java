package io.konig.services;

import io.konig.core.Vertex;

public class VertexAlreadyExistsException extends StorageException {
	private static final long serialVersionUID = 1L;

	public VertexAlreadyExistsException(Vertex v) {
		super("Vertex already exists: " + v.getId().stringValue());
	}

}

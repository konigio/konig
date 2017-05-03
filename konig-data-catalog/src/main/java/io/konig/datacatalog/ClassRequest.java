package io.konig.datacatalog;

import io.konig.core.Vertex;

public class ClassRequest extends PageRequest {

	private Vertex owlClass;
	private ResourceWriterFactory writerFactory;

	public ClassRequest(PageRequest other, Vertex owlClass, ResourceWriterFactory writerFactory) {
		super(other);
		this.owlClass = owlClass;
		this.writerFactory = writerFactory;
	}

	public Vertex getOwlClass() {
		return owlClass;
	}

	public ResourceWriterFactory getWriterFactory() {
		return writerFactory;
	}
	
}

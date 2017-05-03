package io.konig.datacatalog;

import io.konig.core.Vertex;
import io.konig.shacl.ClassManager;
import io.konig.shacl.ClassStructure;

public class ClassRequest extends PageRequest {

	private Vertex owlClass;
	private ResourceWriterFactory writerFactory;

	public ClassRequest(PageRequest other, ResourceWriterFactory writerFactory) {
		super(other);
		this.writerFactory = writerFactory;
	}

	public Vertex getOwlClass() {
		return owlClass;
	}

	public void setOwlClass(Vertex owlClass) {
		this.owlClass = owlClass;
	}

	public ResourceWriterFactory getWriterFactory() {
		return writerFactory;
	}
	
}

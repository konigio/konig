package io.konig.datacatalog;

import io.konig.core.Vertex;
import io.konig.shacl.ClassManager;
import io.konig.shacl.ClassStructure;

public class ClassRequest extends PageRequest {

	private Vertex owlClass;

	public ClassRequest(PageRequest other) {
		super(other);
	}

	public Vertex getOwlClass() {
		return owlClass;
	}

	public void setOwlClass(Vertex owlClass) {
		this.owlClass = owlClass;
	}
	
}

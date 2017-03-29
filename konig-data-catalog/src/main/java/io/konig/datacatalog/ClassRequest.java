package io.konig.datacatalog;

import io.konig.core.Vertex;
import io.konig.shacl.ClassManager;

public class ClassRequest extends PageRequest {

	private ClassManager classManager;
	private Vertex owlClass;

	public ClassRequest(PageRequest other, ClassManager classManager) {
		super(other);
		this.classManager = classManager;
	}

	public ClassManager getClassManager() {
		return classManager;
	}

	public Vertex getOwlClass() {
		return owlClass;
	}

	public void setOwlClass(Vertex owlClass) {
		this.owlClass = owlClass;
	}
	
}

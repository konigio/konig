package io.konig.datagen;

import org.openrdf.model.URI;

public class ClassConstraints {

	private URI targetClass;
	private int instanceCount;
	
	public URI getConstrainedClass() {
		return targetClass;
	}
	public void setConstrainedClass(URI targetClass) {
		this.targetClass = targetClass;
	}
	public int getClassInstanceCount() {
		return instanceCount;
	}
	public void setClassInstanceCount(int instanceCount) {
		this.instanceCount = instanceCount;
	}
	
	
	
	
}

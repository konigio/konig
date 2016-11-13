package io.konig.datagen;

import org.openrdf.model.URI;

public class ClassConstraint {

	private URI targetClass;
	private int instanceCount;
	
	public URI getTargetClass() {
		return targetClass;
	}
	public void setTargetClass(URI targetClass) {
		this.targetClass = targetClass;
	}
	public int getInstanceCount() {
		return instanceCount;
	}
	public void setInstanceCount(int instanceCount) {
		this.instanceCount = instanceCount;
	}
	
	
	
	
}

package io.konig.schemagen.packaging;

public class AssemblyPomConfig {

	private String id;
	private String descriptor;
	
	public AssemblyPomConfig(String id, String descriptor) {
		this.id = id;
		this.descriptor = descriptor;
	}

	public String getId() {
		return id;
	}

	public String getDescriptor() {
		return descriptor;
	}
	
	
	

}

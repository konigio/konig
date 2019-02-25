package io.konig.schemagen.packaging;

public class AssemblyConfig {

	private AssemblyPomConfig pom;
	private AssemblyDescriptorConfig descriptor;
	
	public AssemblyConfig(AssemblyPomConfig pom, AssemblyDescriptorConfig descriptor) {
		this.pom = pom;
		this.descriptor = descriptor;
	}

	public AssemblyPomConfig getPom() {
		return pom;
	}

	public AssemblyDescriptorConfig getDescriptor() {
		return descriptor;
	}
	

}

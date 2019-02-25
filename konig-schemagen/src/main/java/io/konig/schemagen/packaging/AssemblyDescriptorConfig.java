package io.konig.schemagen.packaging;

import java.io.File;

public class AssemblyDescriptorConfig {
	
	private String sourcePath;
	private File descriptorFile;
	
	public AssemblyDescriptorConfig(String sourcePath, File descriptorFile) {
		this.sourcePath = sourcePath;
		this.descriptorFile = descriptorFile;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public File getDescriptorFile() {
		return descriptorFile;
	}


}

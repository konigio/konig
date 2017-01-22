package io.konig.transform;

import io.konig.core.Path;

public class PropertyTransform {

	private Path sourcePath;
	private Path targetPath;
	
	
	
	public PropertyTransform(Path sourcePath, Path targetPath) {
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
	}
	public Path getSourcePath() {
		return sourcePath;
	}
	public Path getTargetPath() {
		return targetPath;
	}
	
	

}

package io.konig.schemagen.maven;

import java.io.File;

public class WorkbookProcessor {
	private File workbookFile;
	private File owlOutDir;
	private File shapesOutDir;
	private boolean inferRdfPropertyDefinitions;
	
	public File getWorkbookFile() {
		return workbookFile;
	}
	public File getOwlOutDir() {
		return owlOutDir;
	}
	public File getShapesOutDir() {
		return shapesOutDir;
	}
	public boolean isInferRdfPropertyDefinitions() {
		return inferRdfPropertyDefinitions;
	}
	public void setInferRdfPropertyDefinitions(boolean inferRdfPropertyDefinitions) {
		this.inferRdfPropertyDefinitions = inferRdfPropertyDefinitions;
	}
	public void setWorkbookFile(File workbookFile) {
		this.workbookFile = workbookFile;
	}
	public void setOwlOutDir(File owlOutDir) {
		this.owlOutDir = owlOutDir;
	}
	public void setShapesOutDir(File shapesOutDir) {
		this.shapesOutDir = shapesOutDir;
	}

}

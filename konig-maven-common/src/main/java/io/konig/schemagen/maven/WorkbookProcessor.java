package io.konig.schemagen.maven;

import java.io.File;

public class WorkbookProcessor {
	private File workbookFile;
	private File owlDir;
	private File shapesDir;
	private boolean inferRdfPropertyDefinitions=true;
	private boolean failOnWarnings = false;
	private boolean failOnErrors = false;
	
	public File getWorkbookFile() {
		return workbookFile;
	}
	public File getOwlDir() {
		return owlDir;
	}
	public File owlDir(RdfConfig defaults) {
		return owlDir == null ? defaults.getOwlDir() : owlDir;
	}
	public File getShapesDir() {
		return shapesDir;
	}
	public File shapesDir(RdfConfig defaults) {
		return shapesDir == null ? defaults.getShapesDir() : shapesDir;
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
	public void setOwlDir(File owlOutDir) {
		this.owlDir = owlOutDir;
	}
	public void setShapesDir(File shapesOutDir) {
		this.shapesDir = shapesOutDir;
	}
	public boolean isFailOnWarnings() {
		return failOnWarnings;
	}
	public void setFailOnWarnings(boolean failOnWarnings) {
		this.failOnWarnings = failOnWarnings;
	}
	public boolean isFailOnErrors() {
		return failOnErrors;
	}
	public void setFailOnErrors(boolean failOnErrors) {
		this.failOnErrors = failOnErrors;
	}

}

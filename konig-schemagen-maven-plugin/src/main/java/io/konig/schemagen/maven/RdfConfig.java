package io.konig.schemagen.maven;

import java.io.File;

public class RdfConfig {
	
	private File rdfDir;
	private File owlDir;
	private File shapesDir;
	
	public File getRdfDir() {
		return rdfDir;
	}
	public void setRdfDir(File rdfDir) {
		this.rdfDir = rdfDir;
	}
	public File getOwlDir() {
		if (owlDir == null && rdfDir!=null) {
			owlDir = new File(rdfDir, "owl");
		}
		return owlDir;
	}
	public void setOwlDir(File owlDir) {
		this.owlDir = owlDir;
	}
	public File getShapesDir() {
		if (shapesDir == null && rdfDir != null) {
			shapesDir = new File(rdfDir, "shapes");
		}
		return shapesDir;
	}
	public void setShapesDir(File shapesDir) {
		this.shapesDir = shapesDir;
	}
	
	public File owlDir(RdfConfig defaults) {
		File result = getOwlDir();
		if (result == null) {
			result = defaults.getOwlDir();
		}
		return result;
	}
	
	public File shapesDir(RdfConfig defaults) {
		File result = getShapesDir();
		if (result == null) {
			result = defaults.getShapesDir();
		}
		return result;
	}
	
}

package io.konig.schemagen.maven;

import java.io.File;

public class RdfConfig {
	
	private File rootDir;
	private File rdfDir;
	private File owlDir;
	private File shapesDir;
	
	public File getRdfDir() {
		if (rdfDir==null && rootDir != null) {
			rdfDir = new File(rootDir, "rdf");
		}
		return rdfDir;
	}
	public void setRdfDir(File rdfDir) {
		this.rdfDir = rdfDir;
	}
	public File getOwlDir() {
		if (owlDir == null && getRdfDir()!=null) {
			owlDir = new File(getRdfDir(), "owl");
		}
		return owlDir;
	}
	public void setOwlDir(File owlDir) {
		this.owlDir = owlDir;
	}
	public File getShapesDir() {
		if (shapesDir == null && getRdfDir() != null) {
			shapesDir = new File(getRdfDir(), "shapes");
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
	
	public File getRootDir() {
		return rootDir;
	}
	public void setRootDir(File rootDir) {
		this.rootDir = rootDir;
	}
	public File shapesDir(RdfConfig defaults) {
		File result = getShapesDir();
		if (result == null) {
			result = defaults.getShapesDir();
		}
		return result;
	}
	
}

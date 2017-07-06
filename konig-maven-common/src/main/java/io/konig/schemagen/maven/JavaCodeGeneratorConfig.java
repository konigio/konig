package io.konig.schemagen.maven;

import java.io.File;

public class JavaCodeGeneratorConfig {

	private File javaDir;
	private String packageRoot;
	private String googleDatastoreDaoPackage;
	private boolean generateCanonicalJsonReaders;
	public File getJavaDir() {
		return javaDir;
	}
	public void setJavaDir(File javaDir) {
		this.javaDir = javaDir;
	}
	
	public String getPackageRoot() {
		return packageRoot;
	}
	public void setPackageRoot(String packageRoot) {
		this.packageRoot = packageRoot;
	}
	public String getGoogleDatastoreDaoPackage() {
		return googleDatastoreDaoPackage;
	}
	public void setGoogleDatastoreDaoPackage(String googleDatastoreDaoPackage) {
		this.googleDatastoreDaoPackage = googleDatastoreDaoPackage;
	}
	public boolean isGenerateCanonicalJsonReaders() {
		return generateCanonicalJsonReaders;
	}
	public void setGenerateCanonicalJsonReaders(boolean generateCanonicalJsonReaders) {
		this.generateCanonicalJsonReaders = generateCanonicalJsonReaders;
	}
	
	
	
}

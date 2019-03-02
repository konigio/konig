package io.konig.maven;

/*
 * #%L
 * Konig Maven Common
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;

public class RdfConfig {
	
	private File rootDir;
	private File rdfDir;
	private File owlDir;
	private File shapesDir;
	private File derivedDir;
	
	
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
	public File getDerivedDir() {
		if (derivedDir == null && getRdfDir()!=null) {
			derivedDir = new File(getRdfDir(), "shape-dependencies");
		}
		return derivedDir;
	}
	public void setDerivedDir(File derivedDir) {
		this.derivedDir = derivedDir;
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
	
}

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

public class WorkbookProcessor {
	private File workbookFile;
	private File workbookDir;
	private File owlDir;
	private File shapesDir;
	private File skosDir;
	
	private boolean inferRdfPropertyDefinitions=true;
	private boolean failOnWarnings = false;
	private boolean failOnErrors = false;
	
	public File getWorkbookFile() {
		return workbookFile;
	}
	public File getOwlDir() {
		return owlDir;
	}
	public File awsDir(RdfConfig defaults) {
		return new File(defaults.getRdfDir(), "aws");
	}	
		
	public File gcpDir(RdfConfig defaults) {
		return new File(defaults.getRdfDir(), "gcp");
	}
	public File owlDir(RdfConfig defaults) {
		return owlDir == null ? defaults.getOwlDir() : owlDir;
	}
	public File skosDir(RdfConfig defaults) {
		return skosDir == null ? defaults.getSkosDir() : skosDir;
		
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
	public File getSkosDir() {
		return skosDir;
	}
	public void setSkosDir(File skosDir) {
		this.skosDir = skosDir;
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
	public File getWorkbookDir() {
		return workbookDir;
	}
	public void setWorkbookDir(File workbookDir) {
		this.workbookDir = workbookDir;
	}
}

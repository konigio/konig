package io.konig.showl;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

public class WorkbookToTurtleRequest {
	private File workbookFile;
	private File owlOutDir;
	private File shapesOutDir;
	private File gcpOutDir;
	private File awsOutDir;
	private File derivedFormOutDir;
	
	public File getDerivedFormOutDir() {
		return derivedFormOutDir;
	}
	public void setDerivedFormOutDir(File derivedFormOutDir) {
		this.derivedFormOutDir = derivedFormOutDir;
	}
	public File getAwsOutDir() {
		return awsOutDir;
	}
	public void setAwsOutDir(File awsOutDir) {
		this.awsOutDir = awsOutDir;
	}
	public File getWorkbookFile() {
		return workbookFile;
	}
	public void setWorkbookFile(File workbookFile) {
		this.workbookFile = workbookFile;
	}
	public File getOwlOutDir() {
		return owlOutDir;
	}
	public void setOwlOutDir(File owlOutDir) {
		this.owlOutDir = owlOutDir;
	}
	public File getShapesOutDir() {
		return shapesOutDir;
	}
	public void setShapesOutDir(File shapesOutDir) {
		this.shapesOutDir = shapesOutDir;
	}
	public File getGcpOutDir() {
		return gcpOutDir;
	}
	public void setGcpOutDir(File gcpOutDir) {
		this.gcpOutDir = gcpOutDir;
	}

}

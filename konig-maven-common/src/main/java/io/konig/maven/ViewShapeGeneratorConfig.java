package io.konig.maven;

/*
 * #%L
 * Konig Maven Common
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


import org.apache.maven.model.FileSet;

public class ViewShapeGeneratorConfig {
	
	private String shapeIriPattern;
	private String shapeIriReplacement;
	private String propertyNamespace;
	private FileSet[] viewFiles;
	
	public String getShapeIriPattern() {
		return shapeIriPattern;
	}
	public String getShapeIriReplacement() {
		return shapeIriReplacement;
	}
	public String getPropertyNamespace() {
		return propertyNamespace;
	}
	public FileSet[] getViewFiles() {
		return viewFiles;
	}
	public void setShapeIriPattern(String shapeIriPattern) {
		this.shapeIriPattern = shapeIriPattern;
	}
	public void setShapeIriReplacement(String shapeIriReplacement) {
		this.shapeIriReplacement = shapeIriReplacement;
	}
	public void setPropertyNamespace(String propertyNamespace) {
		this.propertyNamespace = propertyNamespace;
	}
	public void setViewFiles(FileSet[] viewFiles) {
		this.viewFiles = viewFiles;
	}
}

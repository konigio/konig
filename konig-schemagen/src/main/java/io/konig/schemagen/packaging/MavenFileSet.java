package io.konig.schemagen.packaging;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.util.Collections;
import java.util.List;

public class MavenFileSet {

	private String directory;
	private String outputDirectory;
	private List<String> includes = null;
	
	public MavenFileSet(String directory, String outputDirectory, List<String> includes) {
		this.directory = directory;
		this.outputDirectory = outputDirectory;
		this.includes = includes;
	}

	public String getDirectory() {
		return directory;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public List<String> getIncludes() {
		return includes==null ? Collections.emptyList() : includes;
	}

	public void setIncludes(List<String> includes) {
		this.includes = includes;
	}
	

	
	
}

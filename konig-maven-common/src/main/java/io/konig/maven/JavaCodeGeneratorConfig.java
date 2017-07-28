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
import java.util.List;

public class JavaCodeGeneratorConfig {

	private File javaDir;
	private String packageRoot;
	private String googleDatastoreDaoPackage;
	private boolean generateCanonicalJsonReaders;
	private List<FilterPart> filter;
	
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
	public List<FilterPart> getFilter() {
		return filter;
	}
	public void setFilter(List<FilterPart> filter) {
		this.filter = filter;
	}
	
	
}

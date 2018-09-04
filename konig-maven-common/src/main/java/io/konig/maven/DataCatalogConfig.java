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


public class DataCatalogConfig {

	private String rdfDir;
	private String siteDir;
	private String examplesDir;
	private String ontology;
	private String logFile;
	private boolean showUndefinedClass = false;
	private ContentSystemConfig contentSystem;
	private KonigProject[] dependencies;
	public String getRdfDir() {
		return rdfDir;
	}
	public void setRdfDir(String rdfDir) {
		this.rdfDir = rdfDir;
	}
	public String getSiteDir() {
		return siteDir;
	}
	public void setSiteDir(String siteDir) {
		this.siteDir = siteDir;
	}
	public String getExamplesDir() {
		return examplesDir;
	}
	public void setExamplesDir(String examplesDir) {
		this.examplesDir = examplesDir;
	}
	public String getOntology() {
		return ontology;
	}
	public void setOntology(String ontology) {
		this.ontology = ontology;
	}
	public String getLogFile() {
		return logFile;
	}
	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	public ContentSystemConfig getContentSystem() {
		return contentSystem;
	}
	public void setContentSystem(ContentSystemConfig contentSystem) {
		this.contentSystem = contentSystem;
	}
	public boolean isShowUndefinedClass() {
		return showUndefinedClass;
	}
	public void setShowUndefinedClass(boolean showUndefinedClass) {
		this.showUndefinedClass = showUndefinedClass;
	}
	public KonigProject[] getDependencies() {
		return dependencies;
	}
	public void setDependencies(KonigProject[] dependencies) {
		this.dependencies = dependencies;
	}
	
	

}

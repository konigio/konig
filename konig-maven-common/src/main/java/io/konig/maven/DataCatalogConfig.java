package io.konig.maven;

import org.apache.maven.shared.model.fileset.FileSet;

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
	private ContentSystemConfig contentSystem;
	private FileSet [] sqlFiles;
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
	public FileSet[] getSqlFiles() {
		return sqlFiles;
	}
	public void setSqlFiles(FileSet[] sqlFiles) {
		this.sqlFiles = sqlFiles;
	}
	
	

}

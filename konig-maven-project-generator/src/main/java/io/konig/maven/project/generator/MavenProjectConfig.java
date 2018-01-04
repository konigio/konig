package io.konig.maven.project.generator;

/*
 * #%L
 * Konig Maven Project Generator
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

public class MavenProjectConfig  {
	private File baseDir;
	private String parentId;
	private String groupId;
	private String artifactId;
	private String version;
	private String name;
	private String konigVersion;
	private File rdfSourceDir;
	private String rdfSourcePath;
	private boolean autoBuild;
	
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getArtifactId() {
		return artifactId;
	}
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getKonigVersion() {
		return konigVersion;
	}
	public void setKonigVersion(String konigVersion) {
		this.konigVersion = konigVersion;
	}
	
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public File getBaseDir() {
		return baseDir;
	}
	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}
	public File getRdfSourceDir() {
		return rdfSourceDir;
	}
	public void setRdfSourceDir(File rdfSourceDir) {
		this.rdfSourceDir = rdfSourceDir;
	}
	public String getRdfSourcePath() {
		return rdfSourcePath;
	}
	public void setRdfSourcePath(String rdfSourcePath) {
		this.rdfSourcePath = rdfSourcePath;
	}
	public boolean isAutoBuild() {
		return autoBuild;
	}
	public void setAutoBuild(boolean autoBuild) {
		this.autoBuild = autoBuild;
	}

}

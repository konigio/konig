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


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PackagingProjectRequest {

	private File basedir;
	private MavenProject mavenProject;
	private File velocityLogFile;
	private List<MavenPackage> packages = new ArrayList<>();
	
	public File getBasedir() {
		return basedir;
	}
	public void setBasedir(File basedir) {
		this.basedir = basedir;
	}
	public MavenProject getMavenProject() {
		return mavenProject;
	}
	public void setMavenProject(MavenProject mavenProject) {
		this.mavenProject = mavenProject;
	}
	public File getVelocityLogFile() {
		return velocityLogFile;
	}
	public void setVelocityLogFile(File velocityLogFile) {
		this.velocityLogFile = velocityLogFile;
	}
	
	public PackagingProjectRequest addPackage(MavenPackage pkg) {
		packages.add(pkg);
		return this;
	}
	public List<MavenPackage> getPackages() {
		return packages;
	}
	
	
	

	
}

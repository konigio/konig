package io.konig.core.project;

/*
 * #%L
 * Konig Core
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

public class ProjectFolder {

	private Project project;
	private File localFile;
	
	public ProjectFolder(Project project, File folder) {
		this.project = project;
		this.localFile = folder;
	}

	public ProjectFile createFile(String fileName) {
		File file = new File(localFile, fileName);
		return project.createProjectFile(file);
	}
	
	public boolean exists() {
		return localFile.exists();
	}
	
	public void mkdirs() {
		localFile.mkdirs();
	}

	public File getLocalFile() {
		return localFile;
	}

}

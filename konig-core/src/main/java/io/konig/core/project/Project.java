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

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * A Project that contains a collection of file resources.
 * @author Greg McFall
 *
 */
public class Project {
	private URI id;
	private File baseDir;
	private String baseDirPath;
	
	public static URI createId(String groupId, String artifactId, String version) {

		StringBuilder builder = new StringBuilder();
		builder.append("urn:maven:");
		builder.append(groupId);
		builder.append('.');
		builder.append(artifactId);
		builder.append('-');
		builder.append(version);
		
		return new URIImpl(builder.toString());
	}
	
	public Project(URI id, File baseDir) {
		this.id = id;
		this.baseDir = baseDir;
		baseDirPath = baseDir.getAbsolutePath() + File.separator;
	}
	
	/**
	 * Get a URI that identifies the project as a logical entity.
	 * Typically this will be a URN that identifies a maven project based on the following template:
	 * <pre>
	 *    urn:maven:{groupId}.{artifactId}-{version}
	 * </pre>
	 * 
	 * 
	 * @return
	 */
	public URI getId() {
		return id;
	}
	
	/**
	 * Get the base directory of this Project.  All files in the project are relative to this directory.
	 * @return
	 */
	public File getBaseDir() {
		return baseDir;
	}
	
	public ProjectFile createProjectFile(String path) throws ProjectFileException {
		File file = baseDir==null ? null : new File(baseDir, path);
		return new ProjectFile(this, path, file);
	}
	
	public ProjectFolder createFolder(File localDir) throws ProjectFileException {

		String localPath = localDir.getAbsolutePath();
		if (!localPath.startsWith(baseDirPath)) {
			throw new ProjectFileException("Given file is not contained within the parent project: " + localPath);
		}
		return new ProjectFolder(this, localDir);
		
	}
	
	public ProjectFile createProjectFile(File localFile) throws ProjectFileException {
		
		String localPath = localFile.getAbsolutePath();
		if (!localPath.startsWith(baseDirPath)) {
			throw new ProjectFileException("Given file is not contained within the parent project: " + localPath);
		}
		String relativePath = localPath.substring(baseDirPath.length());
		
		return new ProjectFile(this, relativePath, localFile);
		
	}

	

}

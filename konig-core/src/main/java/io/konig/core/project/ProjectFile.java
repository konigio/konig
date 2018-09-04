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

import io.konig.annotation.RdfProperty;
import io.konig.core.vocab.Konig;

/**
 * A file owned by a given project.
 * @author Greg McFall
 *
 */
public class ProjectFile {
	
	private Project project;
	private String relativePath;
	private File localFile;
	
	ProjectFile(Project project, String relativePath, File localFile) {
		this.project = project;
		this.relativePath = relativePath;
		this.localFile = localFile;
	}
	

	@RdfProperty(Konig.BASE_PROJECT)
	public Project getBaseProject() {
		return project;
	}

	@RdfProperty(Konig.RELATIVE_PATH)
	public String getRelativePath() {
		return relativePath;
	}

	public File getLocalFile() {
		if (localFile==null && project!=null && project.getBaseDir()!=null) {
			localFile = new File(project.getBaseDir(), relativePath);
		}
		return localFile;
	}

	
	

}

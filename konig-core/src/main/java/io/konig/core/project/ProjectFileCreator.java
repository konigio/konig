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


import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.pojo.PojoCreator;
import io.konig.core.vocab.Konig;

public class ProjectFileCreator implements PojoCreator<ProjectFile> {

	private static final Logger logger = LoggerFactory.getLogger(ProjectFileCreator.class);
	
	@Override
	public ProjectFile create(Vertex v) {
		
		Value path = v.getValue(Konig.relativePath);
		if (path == null) {
			throw new KonigException("ProjectFile is missing the `relativePath` property");
		}
		URI projectId = v.getURI(Konig.baseProject);
		if (projectId == null) {
			throw new KonigException("ProjectFile(relativePath: '" + path + "') is missing the `baseProject` property");
		}
		Project project = ProjectManager.instance().getProjectById(projectId);
		if (project == null) {
			logger.warn("Cannot create ProjectFile " + path.toString() + " because the Project <" + projectId + "> is not found.");
			return null;
		}
		
		return project.createProjectFile(path.stringValue());
	}

}

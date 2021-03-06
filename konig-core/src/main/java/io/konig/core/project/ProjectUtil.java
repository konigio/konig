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

public class ProjectUtil {

	public static ProjectFolder createProjectFolder(String projectPath, String folderPath) {
		File projectDir = new File(projectPath);
		URI projectId = new URIImpl(projectDir.toURI().toString());
		Project project = new Project(projectId, projectDir);
		
		File folderDir = new File(projectDir, folderPath);
		return new ProjectFolder(project, folderDir);
	}

}

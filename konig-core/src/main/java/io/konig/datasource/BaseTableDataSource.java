package io.konig.datasource;

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


import io.konig.annotation.RdfProperty;
import io.konig.core.project.ProjectFile;
import io.konig.core.vocab.Konig;

public abstract class BaseTableDataSource extends DataSource implements TableDataSource {

	private ProjectFile ddlFile;
	private ProjectFile transformFile;


	@RdfProperty(Konig.DDL_FILE)
	@Override
	public ProjectFile getDdlFile() {
		return ddlFile;
	}
	
	@Override
	public void setDdlFile(ProjectFile file) {
		ddlFile = file;
	}
	

	@RdfProperty(Konig.TRANSFORM_FILE)
	@Override
	public ProjectFile getTransformFile() {
		return ddlFile;
	}
	
	@Override
	public void setTransformFile(ProjectFile file) {
		ddlFile = file;
	}

}

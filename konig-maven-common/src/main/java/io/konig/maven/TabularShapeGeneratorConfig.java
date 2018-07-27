package io.konig.maven;

/*
 * #%L
 * Konig Maven Common
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


import org.apache.maven.model.FileSet;

/**
 * A common set of configuration used to drive the generation of Tabular
 * Node Shapes given a set of SQL files containing CREATE TABLE or CREATE VIEW statements.
 * @author Greg McFall
 *
 */
public class TabularShapeGeneratorConfig {
	
	private IriTemplateConfig tableIriTemplate;
	private IriTemplateConfig viewIriTemplate;

	private String propertyNamespace;
	private FileSet[] sqlFiles;

	public TabularShapeGeneratorConfig() {
	}

	public IriTemplateConfig getTableIriTemplate() {
		return tableIriTemplate;
	}

	public void setTableIriTemplate(IriTemplateConfig tableIriTemplate) {
		this.tableIriTemplate = tableIriTemplate;
	}

	public IriTemplateConfig getViewIriTemplate() {
		return viewIriTemplate;
	}

	public void setViewIriTemplate(IriTemplateConfig viewIriTemplate) {
		this.viewIriTemplate = viewIriTemplate;
	}

	public String getPropertyNamespace() {
		return propertyNamespace;
	}

	public void setPropertyNamespace(String propertyNamespace) {
		this.propertyNamespace = propertyNamespace;
	}

	public FileSet[] getSqlFiles() {
		return sqlFiles;
	}

	public void setSqlFiles(FileSet[] sqlFiles) {
		this.sqlFiles = sqlFiles;
	}
	
	

}

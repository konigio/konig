package io.konig.maven;

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

package io.konig.maven.datacatalog;

/*
 * #%L
 * Konig Data Catalog Maven Plugin
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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.NamespaceManager;
import io.konig.core.PathFactory;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.datacatalog.DataCatalogBuildRequest;
import io.konig.datacatalog.DataCatalogBuilder;
import io.konig.datacatalog.DataCatalogException;
import io.konig.datasource.DatasourceFileLocator;
import io.konig.datasource.DdlFileLocator;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

@Mojo( name = "generate-site")
public class KonigDataCatalogMojo extends AbstractMojo {

	@Parameter
	private File rdfDir;
	
	@Parameter(defaultValue="${project.basedir}/target/generated/datacatalog")
	private File siteDir;
	
	@Parameter(defaultValue="${project.basedir}/src/examples")
	private File examplesDir;
	
	@Parameter
	private String ontology;
	
	@Parameter(defaultValue="${project.basedir}/target/velocity.log")
	private File logFile;

	@Parameter
	private FileSet[] sqlFiles;
	
	@Parameter
	private boolean showUndefinedClass;
	
	@Component
	private MavenProject mavenProject;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		NamespaceManager nsManager= new MemoryNamespaceManager();
		MemoryGraph graph = new MemoryGraph(nsManager);
		ShapeManager shapeManager = new MemoryShapeManager();
		
		DataCatalogBuilder builder = new DataCatalogBuilder();
		builder.setVelocityLog(logFile);

		PathFactory.RETURN_NULL_ON_FAILURE = true;
		try {
			AwsShapeConfig.init();
			GcpShapeConfig.init();
			RdfUtil.loadTurtle(rdfDir, graph, nsManager);
			ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
			shapeLoader.load(graph);
			URI ontologyId = ontology==null ? null : new URIImpl(ontology);
			
			DataCatalogBuildRequest request = new DataCatalogBuildRequest();
		
			request.setExampleDir(examplesDir);
			request.setGraph(graph);
			request.setOntologyId(ontologyId);
			request.setOutDir(siteDir);
			request.setShapeManager(shapeManager);
			request.setSqlDdlLocator(ddlLocator());
			request.setShowUndefinedClass(showUndefinedClass);
			
			if (ontologyId==null) {
				request.useDefaultOntologyList();
			}
			
			builder.build(request);
			
		} catch (RDFParseException | RDFHandlerException | IOException | DataCatalogException e) {
			throw new MojoExecutionException("Failed to generate DataCatalog site", e);
		} finally {
			PathFactory.RETURN_NULL_ON_FAILURE = false;
		}

	}
	
	private DatasourceFileLocator ddlLocator() throws MojoExecutionException {
		DatasourceFileLocator locator = null;
		if (sqlFiles != null) {
			File sqlDir = createTempSqlFolder();
			FileSetManager manager = new FileSetManager();
			int count = 0;
			
			for (FileSet fileset : sqlFiles) {
				String[] fileList = manager.getIncludedFiles(fileset);
				count += fileList.length;
				for (String filePath : fileList) {
					filePath = 	fileset.getDirectory()+filePath;
					File sourceFile = new File(filePath);
					File targetFile = new File(sqlDir, sourceFile.getName());
					
					try {
						FileUtils.copyFile(sourceFile, targetFile);
					} catch (IOException e) {
						throw new MojoExecutionException("Failed to copy file", e);
					}
				}
			}
			if (count > 0) {
				locator = new DdlFileLocator(sqlDir);
			}
		}
		return locator;
	}

	private File createTempSqlFolder() throws MojoExecutionException {
		File baseDir = mavenProject.getBasedir();
		File sqlDir = new File(baseDir, "target/tmp/sql");
		if (sqlDir.exists()) {
			try {
				FileUtils.deleteDirectory(sqlDir);
			} catch (IOException e) {
				throw new MojoExecutionException("Failed to delete old SQL files", e);
			}
		}
		sqlDir.mkdirs();
		return sqlDir;
	}

	
}

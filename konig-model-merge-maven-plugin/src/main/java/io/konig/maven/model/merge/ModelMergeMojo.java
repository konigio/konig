package io.konig.maven.model.merge;

/*
 * #%L
 * Konig Model Merge Maven Plugin
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


import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.model.MergeException;
import io.konig.model.ModelMerger;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeWriteException;
import io.konig.shacl.io.ShapeWriter;
import io.konig.showl.OntologyFileGetter;
import io.konig.showl.OntologyWriter;

@Mojo( name = "merge")
public class ModelMergeMojo extends AbstractMojo {
	
	@Parameter(required=true)
	private List<ArtifactItem> artifactItems;
	
	@Parameter(defaultValue="${project.basedir}/target/model/merged")
	private File outDir;

	@Parameter(defaultValue="${project.basedir}/target/unpack")
	private File unpackDir;
	
	@Component
	private MavenProject mavenProject;

	@Component
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		if (outDir.exists()) {
			IOUtil.recursiveDelete(outDir);
		}
		outDir.mkdirs();
		unpack();
		
		
		try {

			NamespaceManager nsManager = new MemoryNamespaceManager();
			Graph source = new MemoryGraph(nsManager);
			RdfUtil.loadTurtle(unpackDir, source);
			Graph graph = new MemoryGraph(nsManager);

			ModelMerger merger = new ModelMerger();
			merger.merge(source, graph);
			
			File owlOutDir = new File(outDir, "src/rdf/owl");
			File shapesOutDir = new File(outDir, "src/rdf/shapes");
			owlOutDir.mkdirs();
			shapesOutDir.mkdirs();
			OntologyWriter ontologyWriter = new OntologyWriter(new OntologyFileGetter(owlOutDir, nsManager));
			ontologyWriter.writeOntologies(graph);

			ShapeFileGetter fileGetter = new ShapeFileGetter(shapesOutDir, nsManager);
			ShapeWriter shapeWriter = new ShapeWriter();
			shapeWriter.writeShapes(graph, fileGetter);
			
		} catch (RDFParseException | RDFHandlerException | IOException | MergeException | ShapeWriteException e) {
			throw new MojoExecutionException("Failed to load Turtle", e);
		}

	}

	private void unpack() throws MojoExecutionException {

		for (ArtifactItem item : artifactItems) {
			
			File artifactGroup = new File(unpackDir, item.getGroupId());
			File artifactDir = new File(artifactGroup, item.getArtifactId());
			
			artifactDir.mkdirs();
			
			executeMojo(
				plugin(
					groupId("org.apache.maven.plugins"),
					artifactId("maven-dependency-plugin"),
					version("2.10")
				),
				goal("unpack"),
				configuration(
					element(name("outputDirectory"), artifactDir.getAbsolutePath()),
					element(name("artifactItems"), 
						element(name("artifactItem"), 
							element(name("groupId"), item.getGroupId()),
							element(name("artifactId"), item.getArtifactId()),
							element(name("version"), item.getVersion()),
							element(name("classifier"), "bin"),
							element(name("overWrite"), "true"),
							element(name("includes"), "**/*.ttl"),
							element(name("type"), "zip")
						)
					)
				),
				executionEnvironment(
					mavenProject,
					mavenSession,
					pluginManager
				)
			);
		}
		
	}

}

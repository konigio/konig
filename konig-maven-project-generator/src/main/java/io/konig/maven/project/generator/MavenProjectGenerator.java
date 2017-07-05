package io.konig.maven.project.generator;

/*
 * #%L
 * Konig Maven Project Generator
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

public class MavenProjectGenerator {

	private MavenProjectConfig mavenProject;

	private String templatePath;
	private VelocityEngine engine;
	private VelocityContext context;

	private String artifactSuffix;
	private String nameSuffix;
	
	
	public void init(MavenProjectConfig base) throws MavenProjectGeneratorException  {
		
		if (mavenProject == null) {
			mavenProject = new MavenProjectConfig();
		}
		
		
		if (mavenProject.getGroupId() == null) {
			mavenProject.setGroupId(base.getGroupId());
		}
		if (mavenProject.getArtifactId() == null) {
			mavenProject.setArtifactId(base.getArtifactId() + artifactSuffix);
		}
		if (mavenProject.getVersion() == null) {
			mavenProject.setVersion(base.getVersion());
		}
		if (mavenProject.getName() == null) {
			mavenProject.setName(base.getName() + ' ' +nameSuffix);
		}
		if (mavenProject.getKonigVersion() == null) {
			mavenProject.setKonigVersion(base.getKonigVersion());
		}
		if (mavenProject.getRdfSourceDir() == null) {
			mavenProject.setRdfSourceDir(base.getRdfSourceDir());
		}
		
		if (mavenProject.getKonigVersion()==null) {
			throw new MavenProjectGeneratorException("konigVersion must be defined");
		}

		mavenProject.setBaseDir(new File(base.getBaseDir(), mavenProject.getArtifactId()));
		
	}

	public File getTargetPom() {
		return new File(mavenProject.getBaseDir(), "pom.xml");
	}

	public MavenProjectConfig getMavenProject() {
		return mavenProject;
	}
	
	public void setMavenProject(MavenProjectConfig mavenProject) {
		this.mavenProject = mavenProject;
	}
	
	protected File baseDir() {
		return mavenProject.getBaseDir();
	}

	protected void copyAssembly() throws MavenProjectGeneratorException, IOException {
		
			
			File targetFile = new File(baseDir(), "src/assembly/dep.xml");
			targetFile.getParentFile().mkdirs();
			
			InputStream source = getClass().getClassLoader().getResourceAsStream("konig/generator/assembly/dep.xml");
			FileUtil.copyAndCloseSource(source, targetFile);
			
	}


	public String getArtifactSuffix() {
		return artifactSuffix;
	}

	public void setArtifactSuffix(String artifactSuffix) {
		this.artifactSuffix = artifactSuffix;
	}

	public String getNameSuffix() {
		return nameSuffix;
	}

	public void setNameSuffix(String nameSuffix) {
		this.nameSuffix = nameSuffix;
	}

	public String getTemplatePath() {
		return templatePath;
	}


	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}


	public void run() throws MavenProjectGeneratorException, IOException {
		clean();
		createVelocityEngine();
		createVelocityContext();
		merge();
	}

	private void clean() {
		FileUtil.delete(baseDir());
	}

	private void merge() throws IOException {

		baseDir().mkdirs();
		try (FileWriter out = new FileWriter(getTargetPom())) {
			Template template = engine.getTemplate(templatePath);
			template.merge(context, out);
		}
		
	}


	protected VelocityContext createVelocityContext() {
		context = new VelocityContext();
		context.put("project", mavenProject);
		return context;
	}

	private void createVelocityEngine() {

		File velocityLog = new File("target/logs/" + mavenProject.getArtifactId() + "-velocity.log");
		velocityLog.getParentFile().mkdirs();

		Properties properties = new Properties();
		properties.put("resource.loader", "class");
		properties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		properties.put("runtime.log", velocityLog.getAbsolutePath());

		engine = new VelocityEngine(properties);

	}
	
	public String projectName(MavenProjectConfig baseConfig, String suffix) {
		if (mavenProject != null && mavenProject.getName()!=null) {
			return mavenProject.getName();
		}
		return baseConfig.getName() + ' ' + suffix;
	}

}

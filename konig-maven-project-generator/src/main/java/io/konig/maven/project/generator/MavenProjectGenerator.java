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
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import io.konig.maven.FileUtil;

public class MavenProjectGenerator {

	private MavenProjectConfig mavenProject;

	private String templatePath;
	private VelocityEngine engine;
	private VelocityContext context;

	private String artifactSuffix;
	private String nameSuffix;
	
	public MavenProjectGenerator() {
	}
	
	
	protected void init(MavenProjectConfig base)  {
		
		if (mavenProject == null) {
			mavenProject = new MavenProjectConfig();
		}
		
		File masterBase = base.getBaseDir();
		if (masterBase == null) {
			masterBase = new File(".");
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
		File basedir = new File(base.getBaseDir(), mavenProject.getArtifactId());
		
		if (mavenProject.getRdfSourceDir() == null && base.getRdfSourceDir()!=null) {
			basedir.mkdirs();
			String path = FileUtil.relativePath(basedir, base.getRdfSourceDir());
			mavenProject.setRdfSourceDir(new File(path));
			mavenProject.setRdfSourcePath("${project.basedir}/" + path);
		}

		mavenProject.setBaseDir(basedir);
		
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
	
	protected VelocityContext getContext() {
		return context;
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

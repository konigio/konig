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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import io.konig.maven.FileUtil;
import io.konig.maven.ViewShapeGeneratorConfig;
import io.konig.maven.WorkbookProcessor;

public class RdfModelGenerator extends ConfigurableProjectGenerator<WorkbookProcessor> {
	
	private ViewShapeGeneratorConfig viewShapeGeneratorConfig;
	
	public RdfModelGenerator(MavenProjectConfig mavenProject, WorkbookProcessor workbook) {
		super(workbook, "workbook");
		setTemplatePath("konig/generator/rdf-model/pom.xml");
		setArtifactSuffix("-rdf-model");
		setNameSuffix("RDF Model");
		init(mavenProject);
		
	}
	
	public void setViewShapeGeneratorConfig(ViewShapeGeneratorConfig viewShapeGeneratorConfig) {
		this.viewShapeGeneratorConfig = viewShapeGeneratorConfig;
	}
	
	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		if (config == null) {
			throw new MavenProjectGeneratorException("workbook file must be defined");
		}
		
		File workbookDir = config.getWorkbookDir();
		File workbookFile = config.getWorkbookFile();
		config.setWorkbookFile(localWorkbookFile());
		config.setWorkbookDir(localWorkbookDir());
		super.run();
		config.setWorkbookFile(workbookFile);
		config.setWorkbookDir(workbookDir);
		
		if(viewShapeGeneratorConfig != null) {
			try {
				File rdfPomFile = new File(baseDir(), "pom.xml");
				MavenXpp3Reader mavenreader = new MavenXpp3Reader();
				FileReader reader = new FileReader(rdfPomFile);
				Model model = mavenreader.read(reader);
				model.setPomFile(rdfPomFile);
				Build build = model.getBuild();
				List<Plugin> pluginList = build.getPlugins();
				for (Plugin p : pluginList) {
					if ("konig-schemagen-maven-plugin".equals(p.getArtifactId())) {
						Xpp3Dom conf = (Xpp3Dom) p.getConfiguration();
						Xpp3Dom viewShapeGenerator = conf.getChild("viewShapeGenerator");
						if(viewShapeGenerator == null) {
							conf.addChild(readViewShapeGeneratorConfig(new File("pom.xml")));
						}
					}
				}
				MavenXpp3Writer writer = new MavenXpp3Writer();
				FileWriter fw = new FileWriter(rdfPomFile);
				writer.write(fw, model);
				fw.close();
			} catch (Exception ex) {
				throw new MavenProjectGeneratorException(ex);
			}
		}
		copyAssembly();
		copyWorkbooks();
	}

	private Xpp3Dom readViewShapeGeneratorConfig(File pomFile) throws IOException, XmlPullParserException {
		MavenXpp3Reader mavenreader = new MavenXpp3Reader();
		FileReader reader = new FileReader(pomFile);
		Model model = mavenreader.read(reader);
		model.setPomFile(pomFile);
		Build build = model.getBuild();
		List<Plugin> pluginList = build.getPlugins();
		for (Plugin p : pluginList) {
			if ("konig-schemagen-maven-plugin".equals(p.getArtifactId())) {
				Xpp3Dom conf = (Xpp3Dom) p.getConfiguration();
				Xpp3Dom multiProject = conf.getChild("multiProject");
				return multiProject.getChild("viewShapeGenerator");
			}
		}
		return null;
	}
	
	private File localWorkbookDir() {
		File dir = config.getWorkbookDir();
		return dir==null ? null : new File("src/workbooks");
	}


	private File localWorkbookFile() {
		
		File file = config.getWorkbookFile();
		
		return file==null ? null : new File("src/" + file.getName());
	}


	private void copyWorkbooks() throws MavenProjectGeneratorException, IOException {
		
		File workbookFile = config.getWorkbookFile();
		if (workbookFile != null) {
		
			File targetFile = new File(baseDir(), "src/" + workbookFile.getName());
			targetFile.getParentFile().mkdirs();
			
			FileUtil.copy(config.getWorkbookFile(), targetFile);
		} else {
			File dir = config.getWorkbookDir();
			if (dir == null) {
				throw new MavenProjectGeneratorException("Either workbookFile or workbookDir must be defined");
			}
			
			File targetDir = new File(baseDir(), "src/workbooks");
			targetDir.mkdirs();
			
			File[] list = dir.listFiles();
			for (File file : list) {
				File target = new File(targetDir, file.getName());
				FileUtil.copy(file, target);
			}
		}
	}

	
}

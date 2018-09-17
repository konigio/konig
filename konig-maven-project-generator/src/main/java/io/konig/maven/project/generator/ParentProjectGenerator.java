package io.konig.maven.project.generator;
import java.io.File;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.apache.velocity.VelocityContext;

public class ParentProjectGenerator extends MavenProjectGenerator {

	private List<MavenProjectGenerator> children = new ArrayList<>();
	private File mavenHome;
	private String distributionManagement;
	
	public ParentProjectGenerator(MavenProjectConfig mavenProject) {
		super();
		setTemplatePath("konig/generator/parent/pom.xml");
		setArtifactSuffix("-parent");
		setNameSuffix("Parent");
		init(mavenProject);
	}

	public String getDistributionManagement() {
		return distributionManagement;
	}

	public void setDistributionManagement(String distributionManagement) {
		this.distributionManagement = distributionManagement;
	}



	public void add(MavenProjectGenerator child) {
		if (child != null) {
			String parentId = getMavenProject().getArtifactId();
			child.getMavenProject().setParentId(parentId);
			children.add(child);
		}
	}

	

	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		super.run();
		
		for (MavenProjectGenerator child : children) {
			child.run();
		}
	}

	public void buildChildren(List<String> goalList) throws MavenInvocationException {
		for (MavenProjectGenerator child : children) {
			File mavenHome = mavenHome();
			File childPom = child.getTargetPom();
			InvocationRequest request = new DefaultInvocationRequest();
			request.setPomFile(childPom);
			request.setGoals(goalList);
			
			Invoker invoker = new DefaultInvoker();
			invoker.setMavenHome(mavenHome);
			invoker.execute(request);
		}
		
	}
	
	private File mavenHome() {
		if (mavenHome == null) {

			for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
				File file = new File(dirname, "mvn");
				if (file.isFile()) {
					mavenHome = file.getParentFile().getParentFile();
					return mavenHome;
				}
			}
			throw new RuntimeException("Maven executable not found.");
		}
		return mavenHome;
	}

	@Override
	protected VelocityContext createVelocityContext() {
		VelocityContext context = super.createVelocityContext();
		List<String> moduleNameList = new ArrayList<>();
		for (MavenProjectGenerator child : children) {
			moduleNameList.add(child.getMavenProject().getArtifactId());
		}
		context.put("distributionManagement", distributionManagement);
		context.put("moduleList", moduleNameList);
		return context;
	}
	
	
}

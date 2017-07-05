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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;

public class ParentProjectGenerator extends MavenProjectGenerator {

	private List<MavenProjectGenerator> children = new ArrayList<>();
	
	public ParentProjectGenerator() {
		setTemplatePath("konig/generator/parent/pom.xml");
		setArtifactSuffix("-parent");
		setNameSuffix("Parent");
	}

	public void add(MavenProjectGenerator child) {
		if (child != null) {
			children.add(child);
		}
	}

	@Override
	public void init(MavenProjectConfig base) throws MavenProjectGeneratorException  {
		super.init(base);
		String parentId = getMavenProject().getArtifactId();
		for (MavenProjectGenerator child : children) {
			child.init(base);
			child.getMavenProject().setParentId(parentId);
		}
	}

	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		super.run();
		for (MavenProjectGenerator child : children) {
			child.run();
		}
	}

	@Override
	protected VelocityContext createVelocityContext() {
		VelocityContext context = super.createVelocityContext();
		List<String> moduleNameList = new ArrayList<>();
		for (MavenProjectGenerator child : children) {
			moduleNameList.add(child.getMavenProject().getArtifactId());
		}
		context.put("moduleList", moduleNameList);
		return context;
	}
	
	
}

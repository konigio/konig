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

import org.apache.velocity.VelocityContext;

public class JavaModelGenerator extends MavenProjectGenerator {
	
	private JavaConfig javaConfig;
	
	public JavaModelGenerator() {
		setTemplatePath("konig/generator/javaModel/pom.xml");
		setArtifactSuffix("-java-model");
		setNameSuffix("Java Model");
	}
	
	@Override
	public void init(MavenProjectConfig base) throws MavenProjectGeneratorException {
		super.init(base);
		if (javaConfig == null) {
			javaConfig = new JavaConfig();
		}
		if (javaConfig.getJavaDir() == null) {
			javaConfig.setJavaDir(new File(baseDir(), "src/main/java"));
		}
		if (javaConfig.getPackageRoot() == null) {
			javaConfig.setPackageRoot(base.getGroupId());
		}
	}

	@Override
	public VelocityContext createVelocityContext() {
		VelocityContext context = super.createVelocityContext();
		context.put("java", javaConfig);
		return context;
	}

	public JavaConfig getJava() {
		return javaConfig;
	}

	public void setJava(JavaConfig javaConfig) {
		this.javaConfig = javaConfig;
	}

	
}

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

import io.konig.deploy.gcp.GoogleCloudPlatformInfo;
import io.konig.schemagen.maven.GoogleCloudPlatformConfig;
import io.konig.schemagen.maven.JavaCodeGeneratorConfig;
import io.konig.schemagen.maven.WorkbookProcessor;

public class MultiProject extends MavenProjectConfig {
	
	private WorkbookProcessor workbook;
	private JavaCodeGeneratorConfig java;
	private GoogleCloudPlatformConfig googleCloudPlatform;
	private GoogleCloudPlatformInfo googleCloudPlatformDeployment;
	
	public WorkbookProcessor getWorkbook() {
		return workbook;
	}

	public void setWorkbook(WorkbookProcessor workbook) {
		this.workbook = workbook;
	}

	public JavaCodeGeneratorConfig getJava() {
		return java;
	}

	public void setJava(JavaCodeGeneratorConfig java) {
		this.java = java;
	}

	public GoogleCloudPlatformConfig getGoogleCloudPlatform() {
		return googleCloudPlatform;
	}

	public GoogleCloudPlatformInfo getGoogleCloudPlatformDeployment() {
		return googleCloudPlatformDeployment;
	}

	public void setGoogleCloudPlatformDeployment(GoogleCloudPlatformInfo googleCloudPlatformDeployment) {
		this.googleCloudPlatformDeployment = googleCloudPlatformDeployment;
	}

	public void setGoogleCloudPlatform(GoogleCloudPlatformConfig googleCloudPlatform) {
		this.googleCloudPlatform = googleCloudPlatform;
	}

	public void run() throws MavenProjectGeneratorException, IOException {
		ParentProjectGenerator parent = prepare();
		parent.run();
	}
	
	public ParentProjectGenerator prepare() throws MavenProjectGeneratorException {
		ParentProjectGenerator parent = new ParentProjectGenerator(this);
		if (workbook != null) {
			RdfModelGenerator rdf = new RdfModelGenerator(this, workbook);
			setRdfSourceDir(new File(rdf.baseDir(), "target/generated/rdf"));
			parent.add(rdf);
		}
		if (googleCloudPlatform != null) {
			GoogleCloudPlatformModelGenerator gcp = 
				new GoogleCloudPlatformModelGenerator(this, googleCloudPlatform);
			
			parent.add(gcp);
		}
		if (java != null) {
			parent.add(new JavaModelGenerator(this, java));
		}
		if (googleCloudPlatformDeployment != null) {
			GcpDeployProjectGenerator deploy = new GcpDeployProjectGenerator(this, googleCloudPlatformDeployment);
			parent.add(deploy);
		}
		return parent;
	}
}

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

import io.konig.maven.DataCatalogConfig;
import io.konig.maven.GoogleCloudPlatformConfig;
import io.konig.maven.JavaCodeGeneratorConfig;
import io.konig.maven.JsonSchemaConfig;
import io.konig.maven.WorkbookProcessor;

public class MultiProject extends MavenProjectConfig {
	
	private WorkbookProcessor workbook;
	private JavaCodeGeneratorConfig java;
	private GoogleCloudPlatformConfig googleCloudPlatform;
	private GoogleCloudPlatformConfig googleCloudPlatformDeployment;
	private DataCatalogConfig dataCatalog;
	private JsonSchemaConfig jsonSchema;
	
	public WorkbookProcessor getWorkbook() {
		return workbook;
	}
	
	

	public JsonSchemaConfig getJsonSchemaConfig() {
		return jsonSchema;
	}
	
	public void setJsonSchemaConfig(JsonSchemaConfig jsonSchemaConfig) {
		this.jsonSchema = jsonSchemaConfig;
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

	public GoogleCloudPlatformConfig getGoogleCloudPlatformDeployment() {
		return googleCloudPlatformDeployment;
	}

	public void setGoogleCloudPlatformDeployment(GoogleCloudPlatformConfig googleCloudPlatformDeployment) {
		this.googleCloudPlatformDeployment = googleCloudPlatformDeployment;
	}

	public void setGoogleCloudPlatform(GoogleCloudPlatformConfig googleCloudPlatform) {
		this.googleCloudPlatform = googleCloudPlatform;
	}

	public ParentProjectGenerator run() throws MavenProjectGeneratorException, IOException {
		ParentProjectGenerator parent = prepare();
		parent.run();
		return parent;
	}
	
	public ParentProjectGenerator prepare() throws MavenProjectGeneratorException {
		ParentProjectGenerator parent = new ParentProjectGenerator(this);
		if (workbook != null) {
			RdfModelGenerator rdf = new RdfModelGenerator(this, workbook);
			setRdfSourceDir(new File(rdf.baseDir(), "target/generated/rdf"));
			parent.add(rdf);
		}
		if (googleCloudPlatform != null) {
			
			GoogleCloudPlatformConfig appEngine = null;
			if (googleCloudPlatform.getDataServices() != null) {
				appEngine = new GoogleCloudPlatformConfig();
				appEngine.setDataServices(googleCloudPlatform.getDataServices());
				appEngine.setEnableBigQueryTransform(false);
				googleCloudPlatform.setDataServices(null);
			}
			
			GoogleCloudPlatformModelGenerator gcp = 
				new GoogleCloudPlatformModelGenerator(this, googleCloudPlatform);
			
			parent.add(gcp);
			if (appEngine != null) {
				parent.add(new AppEngineGenerator(this, appEngine));
			}
		}
		if (java != null) {
			parent.add(new JavaModelGenerator(this, java));
		}
		if (jsonSchema != null) {
			parent.add(new JsonSchemaGenerator(this, jsonSchema));
		}
		if (googleCloudPlatformDeployment != null) {
			GcpDeployProjectGenerator deploy = new GcpDeployProjectGenerator(this, googleCloudPlatformDeployment);
			parent.add(deploy);
		}
		if (dataCatalog != null) {
			parent.add(new DataCatalogProjectGenerator(this, dataCatalog));
		}
		return parent;
	}
}

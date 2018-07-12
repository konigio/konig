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

import org.apache.maven.shared.model.fileset.FileSet;

import io.konig.maven.AmazonWebServicesConfig;
import io.konig.maven.DataCatalogConfig;
import io.konig.maven.GoogleCloudPlatformConfig;
import io.konig.maven.JavaCodeGeneratorConfig;
import io.konig.maven.JsonSchemaConfig;
import io.konig.maven.OracleManagedCloudConfig;
import io.konig.maven.TabularShapeGeneratorConfig;
import io.konig.maven.WorkbookProcessor;

public class MultiProject extends MavenProjectConfig {
	
	private WorkbookProcessor workbook;
	private JavaCodeGeneratorConfig java;
	private GoogleCloudPlatformConfig googleCloudPlatform;
	private GoogleCloudPlatformConfig googleCloudPlatformDeployment;
	private DataCatalogConfig dataCatalog;
	private JsonSchemaConfig jsonSchema;
	private OracleManagedCloudConfig oracleManagedCloud;
	private AmazonWebServicesConfig amazonWebServices;	
	private TabularShapeGeneratorConfig tabularShapeGenerator;
	 
	public WorkbookProcessor getWorkbook() {
		return workbook;
	}
	
	public OracleManagedCloudConfig getOracleManagedCloudConfig() {
		return oracleManagedCloud;
	}
	
	public void setOracleManagedCloudConfig(OracleManagedCloudConfig oracleManagedCloud) {
		this.oracleManagedCloud = oracleManagedCloud;
	}

	public AmazonWebServicesConfig getAmazonWebServicesConfig() {
		return amazonWebServices;
	}
	
	public void setAmazonWebServicesConfig(AmazonWebServicesConfig amazonWebServices) {
		this.amazonWebServices = amazonWebServices;
	}

	public JsonSchemaConfig getJsonSchemaConfig() {
		return jsonSchema;
	}
	
	public TabularShapeGeneratorConfig getTabularShapeGeneratorConfig() {
		return tabularShapeGenerator;
	}
	
	public void setTabularShapeGeneratorConfig(TabularShapeGeneratorConfig tabularShapeGenerator) {
		this.tabularShapeGenerator = tabularShapeGenerator;
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
			rdf.setTabularShapeGeneratorConfig(tabularShapeGenerator);
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
		if(amazonWebServices != null) {
			parent.add(new AwsModelGenerator(this, amazonWebServices));
		}
		if (dataCatalog != null) {
			dataCatalog.setSqlFiles(sqlFileSet());
			parent.add(new DataCatalogProjectGenerator(this, dataCatalog));
		}
		if(oracleManagedCloud != null) {
			parent.add(new OracleManagedCloudProjectGenerator(this, oracleManagedCloud));
		}
		
		return parent;
	}

	private FileSet[] sqlFileSet() {
		
		List<FileSet> list = new ArrayList<>();
		
		addGoogleFileSet(list);
		addAwsFileSet(list);
		
		FileSet[] array = null;
		
		if (!list.isEmpty()) {
			array = new FileSet[list.size()];
			list.toArray(array);
		}
		
		return array;
	}

	private void addAwsFileSet(List<FileSet> list) {
		if (amazonWebServices != null) {
			FileSet fileSet = new FileSet();
			fileSet.setDirectory("../" + getArtifactId() + AwsModelGenerator.ARTIFACT_SUFFIX + AwsModelGenerator.TABLES_PATH+"/");
			fileSet.addInclude("*.sql");
			list.add(fileSet);
		}
		
	}

	private void addGoogleFileSet(List<FileSet> list) {
		if (googleCloudPlatform != null) {
			FileSet fileSet = new FileSet();
			fileSet.setDirectory("../" + getArtifactId() + GoogleCloudPlatformModelGenerator.ARTIFACT_SUFFIX 
					+ GoogleCloudPlatformModelGenerator.CLOUD_SQL_PATH+"/");
			fileSet.addInclude("*.sql");
			list.add(fileSet);
		}
		
	}



	
}

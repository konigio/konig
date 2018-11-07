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

import io.konig.core.project.Project;
import io.konig.maven.AmazonWebServicesConfig;
import io.konig.maven.BuildTarget;
import io.konig.maven.DataCatalogConfig;
import io.konig.maven.GoogleCloudPlatformConfig;
import io.konig.maven.JavaCodeGeneratorConfig;
import io.konig.maven.JsonSchemaConfig;
import io.konig.maven.KonigProject;
import io.konig.maven.ModelValidationConfig;
import io.konig.maven.OracleManagedCloudConfig;
import io.konig.maven.OwlInference;
import io.konig.maven.OwlProfile;
import io.konig.maven.ParentProjectConfig;
import io.konig.maven.TabularShapeFactoryConfig;
import io.konig.maven.TabularShapeGeneratorConfig;
import io.konig.maven.WorkbookProcessor;

public class MultiProject extends MavenProjectConfig {
	
	private ParentProjectConfig parentProject;
	private WorkbookProcessor workbook;
	private JavaCodeGeneratorConfig java;
	private GoogleCloudPlatformConfig googleCloudPlatform;
	private GoogleCloudPlatformConfig googleCloudPlatformDeployment;
	private DataCatalogConfig dataCatalog;
	private JsonSchemaConfig jsonSchema;
	private OracleManagedCloudConfig oracleManagedCloud;
	private AmazonWebServicesConfig amazonWebServices;	
	private TabularShapeGeneratorConfig tabularShapeGenerator;
	private TabularShapeFactoryConfig tabularShapes;
	private ModelValidationConfig modelValidation;
	private OwlProfile[] profiles;
	private OwlInference[] inferences;
	private BuildTarget buildTarget;
	
	public MultiProject() {
		
	}
	 
	public TabularShapeFactoryConfig getTabularShapes() {
		return tabularShapes;
	}


	public void setTabularShapes(TabularShapeFactoryConfig tabularShapes) {
		this.tabularShapes = tabularShapes;
	}


	public WorkbookProcessor getWorkbook() {
		return workbook;
	}
	
	public ParentProjectConfig getParentProject() {
		return parentProject;
	}

	public void setParentProject(ParentProjectConfig parentProject) {
		this.parentProject = parentProject;
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
		if (parentProject != null) {
			parent.setDistributionManagement(parentProject.getDistributionManagement());
		}
		if (workbook != null) {
			RdfModelGenerator rdf = new RdfModelGenerator(this, workbook);
			rdf.setTabularShapeGeneratorConfig(tabularShapeGenerator);
			rdf.setTabularShapeFactoryConfig(tabularShapes);
			rdf.setModelValidationConfig(modelValidation);
			rdf.setProfiles(profiles);
			rdf.setInferences(inferences());
			rdf.setGoogleCloudPlatform(googleCloudPlatform);
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
			dataCatalog.setDependencies(catalogDependencies());
			dataCatalog.setRdfSources(rdfSources());
			
			parent.add(new DataCatalogProjectGenerator(this, dataCatalog));
		}
		if(oracleManagedCloud != null) {
			parent.add(new OracleManagedCloudProjectGenerator(this, oracleManagedCloud));
		}
		
		return parent;
	}
	
	private OwlInference[] inferences() {
		if (inferences == null) {
			inferences = new OwlInference[] {
				OwlInference.OWL_PROPERTY_CLASSIFICATION	
			};
		}
		return inferences;
	}

	private String[] rdfSources() {
		List<String> list = new ArrayList<>();

		addRdfSource(list, amazonWebServices, AwsModelGenerator.ARTIFACT_SUFFIX, "/target/generated");
		addRdfSource(list, googleCloudPlatform, GoogleCloudPlatformModelGenerator.ARTIFACT_SUFFIX, "/target/generated");
		
		if (!list.isEmpty()) {
			String[] array = new String[list.size()];
			list.toArray(array);
			return array;
		}
		return null;
	}

	private void addRdfSource(List<String> list, Object config, String artifactSuffix, String path) {
		if (config != null) {
			String source = "../" + getArtifactId() + artifactSuffix + path ;
			list.add(source);
		}
		
	}

	private KonigProject[] catalogDependencies() {
		List<KonigProject> list = new ArrayList<>();
		
		addProject(list, amazonWebServices, AwsModelGenerator.ARTIFACT_SUFFIX);
		addProject(list, googleCloudPlatform, GoogleCloudPlatformModelGenerator.ARTIFACT_SUFFIX);
		
		KonigProject[] array = null;
		if (!list.isEmpty()) {
			array = new KonigProject[list.size()];
			list.toArray(array);
		}
		return array;
	}

	private void addProject(List<KonigProject> list, Object config,	String artifactSuffix) {
		if (config != null) {
			String groupId = getGroupId();
			String artifactId = getArtifactId() + artifactSuffix;
			String version = getVersion();
			
			String baseDir = "../" + getArtifactId() + artifactSuffix;
			
			String id = Project.createId(groupId, artifactId, version).stringValue();
			
			list.add(new KonigProject(id, baseDir));
		}
		
	}

	public ModelValidationConfig getModelValidation() {
		return modelValidation;
	}

	public void setModelValidation(ModelValidationConfig modelValidation) {
		this.modelValidation = modelValidation;
	}

	public OwlProfile[] getProfiles() {
		return profiles;
	}

	public void setProfiles(OwlProfile[] profiles) {
		this.profiles = profiles;
	}

	public OwlInference[] getInferences() {
		return inferences;
	}

	public void setInferences(OwlInference[] inferences) {
		this.inferences = inferences;
	}

	public BuildTarget getBuildTarget() {
		return buildTarget;
	}

	public void setBuildTarget(BuildTarget buildTarget) {
		this.buildTarget = buildTarget;
	}

	

	
}

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


import java.io.IOException;

import io.konig.maven.BigQueryInfo;
import io.konig.maven.CloudSqlInfo;
import io.konig.maven.GoogleCloudPlatformConfig;

public class GoogleCloudPlatformModelGenerator extends ConfigurableProjectGenerator<GoogleCloudPlatformConfig> {

	public static final String ARTIFACT_SUFFIX = "-gcp-model";
	public static final String CLOUD_SQL_PATH = "/target/generated/gcp/cloudsql/tables";

	public GoogleCloudPlatformModelGenerator(
		MavenProjectConfig mavenProject,
		GoogleCloudPlatformConfig config
	) {
		super(config, "googleCloudPlatform");

		setTemplatePath("konig/generator/gcpModel/pom.xml");
		setArtifactSuffix(ARTIFACT_SUFFIX);
		setNameSuffix("Google Cloud Platform Model");
		init(mavenProject);
		BigQueryInfo bigQueryInfo =new BigQueryInfo();
		CloudSqlInfo cloudSqlInfo = new CloudSqlInfo();
		if (bigQueryInfo != null){
		bigQueryInfo.setShapeIriPattern("(.*)Shape$");
		bigQueryInfo.setShapeIriReplacement("$1RdbmsShape");
		bigQueryInfo.setPropertyNameSpace("http://example.com/ns/alias/");
		config.setBigquery(bigQueryInfo);
		}
		if (cloudSqlInfo != null){
		cloudSqlInfo.setShapeIriPattern("(.*)Shape$");
		cloudSqlInfo.setShapeIriReplacement("$1RdbmsShape");
		cloudSqlInfo.setPropertyNameSpace("http://example.com/ns/alias/");
		config.setCloudsql(cloudSqlInfo);
		}
	}


	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		super.run();
		copyAssembly();
	}
}

package io.konig.gcp.common;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
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
import java.io.Writer;
import java.text.MessageFormat;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableInfo;

import io.konig.maven.FileUtil;
import io.konig.maven.GoogleCloudPlatformConfig;

public class GroovyDeploymentScriptWriter {

	private String konigVersion;
	private GoogleCloudPlatformConfig googleCloudInfo;
	private GoogleCloudService googleCloudService;
	private File scriptFile;
	private Writer out;
	private String indent = "   ";
	
	


	public GroovyDeploymentScriptWriter(String konigVersion, GoogleCloudPlatformConfig googleCloudInfo,
			GoogleCloudService googleCloudService, File scriptFile) {
		this.konigVersion = konigVersion;
		this.googleCloudInfo = googleCloudInfo;
		this.googleCloudService = googleCloudService;
		this.scriptFile = scriptFile;
	}


	public void run() throws IOException {
		
		scriptFile.getParentFile().mkdirs();
		
		try (FileWriter writer = new FileWriter(scriptFile)) {
			out = writer;
			
			String baseDir = FileUtil.relativePath(scriptFile, googleCloudInfo.getDirectory());

			String credentialsPath = googleCloudService.getCredentialsFile().getCanonicalPath();
			String grab = MessageFormat.format("@Grab('io.konig:konig-gcp-deploy-maven-plugin:{0}')", konigVersion);
			
			String delegate = MessageFormat.format("deploymentPlan.delegate = new KonigDeployment(\"{0}\", \"{1}\")", 
					credentialsPath, baseDir);
			
			println(grab);
			println();
			println("import static io.konig.deploy.ResourceType.*;");
			println("import io.konig.maven.deploy.KonigDeployment;");
			println();
			println("def deploymentPlan = {");
			printDatasetCommands();
			printTableCommands();
			println("}");
			println(delegate);
			println("deploymentPlan()");
		}
		
	}


	private void printTableCommands() throws IOException {

		File schemaDir = googleCloudInfo.getBigquery().getSchema();
		if (schemaDir != null) {
			BigQuery bigquery = googleCloudService.bigQuery();
			for (File file : schemaDir.listFiles()) {
				TableInfo info = googleCloudService.readTableInfo(file);
				Table table = bigquery.getTable(info.getTableId());
				if (table != null) {
					String path = FileUtil.relativePath(scriptFile, file);
					print(indent);
					print("create BigQueryTable from \"");
					print(path);
					println("\"");
				}
			}
		}
		
	}


	private void printDatasetCommands() throws IOException {
		
		File datasetDir = googleCloudInfo.getBigquery().getDataset();
		if (datasetDir != null) {
			BigQuery bigquery = googleCloudService.bigQuery();
			for (File file : datasetDir.listFiles()) {
				DatasetInfo info = googleCloudService.readDatasetInfo(file);
				Dataset dataset = bigquery.getDataset(info.getDatasetId());
				if (dataset == null) {
					String path = FileUtil.relativePath(scriptFile, file);
					print(indent);
					print("create BigQueryDataset from \"");
					print(path);
					println("\"");
				}
			}
		}
		
	}

	private void print(String text) throws IOException {
		out.write(text);
	}

	private void println() throws IOException {
		out.write('\n');
	}
	private void println(String text) throws IOException {
		out.write(text);
		out.write('\n');
		
	}
	
	
	

}

package io.konig.gcp.common;

import java.io.BufferedReader;

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
import java.io.FileReader;
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

public class GroovyTearDownScriptWriter {

	private String konigVersion;
	private GoogleCloudPlatformConfig googleCloudInfo;
	private GoogleCloudService googleCloudService;
	private File scriptFile;
	private Writer out;
	private String indent = "   ";
	
	


	public GroovyTearDownScriptWriter(String konigVersion, GoogleCloudPlatformConfig googleCloudInfo,
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
			//String baseDir = ".";

			String credentialsPath = googleCloudService.getCredentialsFile().getCanonicalPath().replace('\\', '/');
			String grab = MessageFormat.format("@Grab(\"io.konig:konig-gcp-deploy-maven-plugin:{0}\")", konigVersion);
			
			String delegate = MessageFormat.format("deploymentPlan.delegate = new KonigDeployment(\"{0}\",  scriptDir)", 
					credentialsPath);
			
			println(grab);
			println();			
			println("import static io.konig.maven.InsertResourceType.*;");
			println("import static io.konig.maven.ResourceType.*;");
			println("import io.konig.maven.KonigDeployment;");
			println();
			println("def deploymentPlan = {");
			printTableCommands();
			printDatasetCommands();
			printGooglePubSubCommands();
			println("}");
			println("def scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent");
			println(delegate);
			println("deploymentPlan()");
		}
		
	}

	private void printGooglePubSubCommands() throws IOException {
		
		File topicsFile = googleCloudInfo.getTopicsFile();
		if (topicsFile != null && topicsFile.exists()) {
			try (
				FileReader fileReader = new FileReader(topicsFile);
				BufferedReader reader = new BufferedReader(fileReader);
			) {
				String line;
				while ((line=reader.readLine()) != null) {
					String topicName = line.trim();
					if (topicName.length()>0) {
						print(indent);
						print("delete GooglePubSubTopic named \"");
						print(topicName);
						print("\"");
						println(" println response ");
					}
				}
				
			}
		}
		
	}


	private void printTableCommands() throws IOException {

		File schemaDir = googleCloudInfo.getBigquery().getSchema();
		if (schemaDir != null) {
			BigQuery bigquery = googleCloudService.bigQuery();
			for (File file : schemaDir.listFiles()) {
				TableInfo info = googleCloudService.readTableInfo(file);
				Table table = bigquery.getTable(info.getTableId());
				if (table == null) {
					String path = FileUtil.relativePath(scriptFile, file);
					print(indent);
					print("delete BigQueryTable from \"");
					print(path);
					print("\"");
					println(" println response ");
				}
			}
		}
		
	}


	private void printDatasetCommands() throws IOException {
		
		File datasetDir = googleCloudInfo.getBigquery().getDataset();
		if (datasetDir != null && datasetDir.isDirectory()) {
			BigQuery bigquery = googleCloudService.bigQuery();
			for (File file : datasetDir.listFiles()) {
				DatasetInfo info = googleCloudService.readDatasetInfo(file);
				Dataset dataset = bigquery.getDataset(info.getDatasetId());
				if (dataset == null) {
					String path = FileUtil.relativePath(scriptFile, file);
					print(indent);
					print("delete BigQueryDataset from \"");
					print(path);
					print("\"");
					println(" println response ");
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
package io.konig.aws.common;

/*
 * #%L
 * Konig AWS Common
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

import io.konig.maven.AmazonWebServicesConfig;
import io.konig.maven.AuroraInfo;
import io.konig.maven.FileUtil;

public class GroovyAwsTearDownScriptWriter {
	private String konigVersion;
	private AmazonWebServicesConfig amazonWebService;
	private File scriptFile;
	private Writer out;
	private String indent = "   ";
	
	public GroovyAwsTearDownScriptWriter(AmazonWebServicesConfig auroraManagedCloudConfig) {
		this.amazonWebService = auroraManagedCloudConfig;
		this.konigVersion = auroraManagedCloudConfig.getKonigVersion();
		this.scriptFile = auroraManagedCloudConfig.getTearDownScriptFile();
	}

	public void run() throws IOException {
		scriptFile.getParentFile().mkdirs();
		
		try (FileWriter writer = new FileWriter(scriptFile)) {
			out = writer;
			String grab = MessageFormat.format("@Grab(\"io.konig:konig-aws-deploy-maven-plugin:{0}\")", konigVersion);
			println(grab);
			println();			
			println("import static io.konig.maven.AwsResourceType.*;");
			println("import io.konig.maven.AwsDeployment;");
			println();
			println("def deploymentPlan = {");
			printCloudFormationStackCommands();
			printTableViewCommands();
			printTableCommands();
			printAmazonBucketCommands();
			println("}");
			println("def scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent");
			println("deploymentPlan.delegate = new AwsDeployment(scriptDir)");
			println("deploymentPlan()");
			
		}
		
	}

	private void printTableViewCommands() throws IOException {
		File viewDir = amazonWebService.getAurora().getViews();
		
		if (viewDir != null && viewDir.exists()) {
			for (File file : viewDir.listFiles()) {
				if (file.getName().endsWith(".json")) {
					String path = FileUtil.relativePath(scriptFile, file);
					print(indent);
					print("delete AwsAuroraView from \"");
					print(path);
					println("\"");
					println(" println response ");
				}
			}
		}
		
	}

	private void printCloudFormationStackCommands() throws IOException {
		File schemaDir = amazonWebService.getCloudFormationTemplates();
		if (schemaDir != null && schemaDir.exists()) {
			for (File file : schemaDir.listFiles()) {
				if (file.getName().endsWith(".json")) {
					String path = FileUtil.relativePath(scriptFile, file);
					print(indent);
					print("delete CloudFormationStack from \"");
					print(path);
					print("\"");
					println(" println response ");
				}
			}
		}
		
	}

	private void printTableCommands() throws IOException {
		AuroraInfo aurora=amazonWebService.getAurora();
		File schemaDir=null;
		if(aurora!=null)
			schemaDir = aurora.getTables();
		if (schemaDir != null && schemaDir.exists()) {
			for (File file : schemaDir.listFiles()) {
				if (file.getName().endsWith(".json")) {
					String path = FileUtil.relativePath(scriptFile, file);
					print(indent);
					print("delete AwsAurora from \"");
					print(path);
					print("\"");
					println(" println response ");
				}
			}
		}
	}
	

	private void printAmazonBucketCommands() throws IOException {
		File schemaDir = amazonWebService.getS3buckets();
		if (schemaDir!=null && schemaDir.exists()) {
			File[] fileList = schemaDir.listFiles();
			for (File file : fileList) {
				 String path = FileUtil.relativePath(scriptFile, file);
				 	
				   print(indent);
				   print("delete AwsSnsTopic from \"");
				   print(path);
				   print("\"");
				   println(" println response ");
				   	
				   print(indent);
				   print("delete AwsSqsQueue from \"");
				   print(path);
				   print("\"");
				   println(" println response ");
				   
					print(indent);
					print("delete AwsS3Bucket from \"");
					print(path);
					print("\"");
					println(" println response ");
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

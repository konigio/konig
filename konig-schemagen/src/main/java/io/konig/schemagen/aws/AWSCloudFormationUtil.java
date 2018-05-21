package io.konig.schemagen.aws;

/*
 * #%L
 * Konig Schema Generator
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.DescribeStackEventsRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudformation.model.StackEvent;
import com.amazonaws.services.cloudformation.model.StackStatus;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.amazonaws.services.cloudformation.model.ValidateTemplateResult;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.konig.aws.common.InvalidAWSCredentialsException;
import io.konig.aws.common.StackCreationException;
import io.konig.spreadsheet.DataSourceGenerator.VelocityFunctions;

public class AWSCloudFormationUtil {
	
	public static class VelocityFunctions {
		private File path;
		public VelocityFunctions(File cfDir) {
			this.path = cfDir;
		}
		private void readTags(String fileName, StringBuffer template) throws FileNotFoundException, IOException {
			File resourceFile = new File(path, fileName);
			if(resourceFile.exists()){
				try (InputStream inputStream = new FileInputStream(resourceFile)) {
					String contents = IOUtils.toString(inputStream);
					YAMLMapper mapper = new YAMLMapper(new YAMLFactory());
					JsonNode node = mapper.readTree(contents);
					JsonNode resourcesNode = node.get("Tags");
					String jsonAsYaml = new YAMLMapper().writeValueAsString(resourcesNode);
					String[] resourceLines=jsonAsYaml.split("\n");
					for(String line:resourceLines){
						if(!line.contains("---")){
							template.append(""+line+"\n      ");
						}
					}
				}
			}
		}
		
		public String mergeSecurityTags(String text) throws FileNotFoundException, IOException {
			StringBuffer template=new StringBuffer();
			readTags(text + ".yml", template);
			readTags("arn.all.yml", template);
			return template.toString().trim();
			
		}
	}
	
	public static void writeCloudFormationTemplate(File cfDir,String template, boolean validate) throws IOException, ParseException {
		
		for(File file:cfDir.listFiles()){
			if(file.getName().endsWith("_template.yml")){
				String contents = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
				
				VelocityContext context = new VelocityContext();
				context.put("functions", new VelocityFunctions(cfDir));
				StringWriter tempresult = new StringWriter();
				Template vmtemplate = new Template();
				RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
				StringReader reader = new StringReader(contents);
				vmtemplate.setRuntimeServices(runtimeServices);
				vmtemplate.setData(runtimeServices.parse(reader, template));
				vmtemplate.initDocument();
				vmtemplate.merge(context, tempresult);
				contents = tempresult.toString();
				YAMLMapper mapper = new YAMLMapper(new YAMLFactory());
				JsonNode node = mapper.readTree(contents);
				JsonNode outputNode=node.get("Outputs");
				if(outputNode!=null){			
					String outputs=contents.substring(contents.lastIndexOf("Outputs:"));
					String resources=contents.substring(0,contents.lastIndexOf("Outputs:"));
						resources=resources+template;
					contents=resources+outputs;
				}
				
				AmazonCloudFormationClient client = new AmazonCloudFormationClient();
				ValidateTemplateRequest request = new ValidateTemplateRequest();
				request.setTemplateBody(contents);	
				if (validate) {
					ValidateTemplateResult result=client.validateTemplate(request);
				}
				
				try(FileWriter fileWriter= new FileWriter(file)){
					fileWriter.write(contents);
				}
			}
		}

	}
	public static String getResourcesAsString(Map<String, Object> resources) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());	
		mapper.setSerializationInclusion(Include.NON_NULL);
		String resource=mapper.writeValueAsString(resources);
		resource = resource.replaceAll("\"!Ref ([^\\s]+)\"", "!Ref $1"); 

		String[] resourceLines=resource.split("\n");
		StringBuffer template=new StringBuffer();
		for(String line:resourceLines){
			if(!line.contains("---")){
				template.append("  "+line+"\n");
			}
		}
		return template.toString();
	}
	
	public static void verifyAWSCredentials() throws InvalidAWSCredentialsException {
		String accessKeyId = System.getProperty("aws.accessKeyId");
		String secretKey = System.getProperty("aws.secretKey");
		if (accessKeyId == null || secretKey == null)
			throw new InvalidAWSCredentialsException();
	}
	public static AWSStaticCredentialsProvider getCredential() throws InvalidAWSCredentialsException {
		verifyAWSCredentials();
		return new AWSStaticCredentialsProvider(
				new BasicAWSCredentials(System.getProperty("aws.accessKeyId"), System.getProperty("aws.secretKey")));

	}
}

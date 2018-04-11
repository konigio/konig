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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.ValidateTemplateRequest;
import com.amazonaws.services.cloudformation.model.ValidateTemplateResult;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;


public class AWSCloudFormationUtil {
	
	public static void writeCloudFormationTemplate(File cfDir,String template, boolean validate) throws IOException {
		for(File file:cfDir.listFiles()){
			String contents = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
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
	public static String getResourcesAsString(Map<String, Object> resources) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());	
		mapper.setSerializationInclusion(Include.NON_NULL);
		String resource=mapper.writeValueAsString(resources);
		for(String key:resources.keySet()){
			if(key.endsWith("SNSTopic") || key.endsWith("SQSQueue") || key.endsWith("DBCluster")){
				resource=resource.replace("\"!Ref "+key+"\"", "!Ref "+key);
			}
		}

		String[] resourceLines=resource.split("\n");
		StringBuffer template=new StringBuffer();
		for(String line:resourceLines){
			if(!line.contains("---")){
				template.append("  "+line+"\n");
			}
		}
		return template.toString();
	}
}

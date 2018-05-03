package io.konig.maven;

/*
 * #%L
 * Konig AWS Deployment Maven Plugin
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
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import io.konig.aws.datasource.AwsAurora;
import io.konig.aws.datasource.AwsAuroraDefinition;
import io.konig.aws.datasource.AwsAuroraTableReference;

public class CreateAuroraTableAction {
	
	private AwsDeployment deployment;

	public CreateAuroraTableAction(AwsDeployment deployment) {
		this.deployment = deployment;
	}
	
	public AwsDeployment from(String path) throws Exception {
		Connection connection = null;
		Statement statement = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			File file = deployment.file(path);
			AwsAuroraDefinition tableDefinition = mapper.readValue(file, AwsAuroraDefinition.class);
			AwsAuroraTableReference table = tableDefinition.getTableReference();
			String awsHost = table.getAwsAuroraHost();
			
			if(System.getProperty(awsHost) != null) {
				awsHost = System.getProperty(awsHost);
			}
			else{
				
				File dir=new File(file.getParentFile().getParent()+"/cloudformationtemplate");
				if(dir!=null && dir.exists() && dir.isDirectory()){
					File[] files=dir.listFiles();
					File outputFile=null;
					for(File f:files){
						if(f.getName().endsWith("output.json")){
							outputFile=f;
							break;
						}
					}
					ObjectMapper objMapper=new ObjectMapper();
					JsonNode jsonNode=objMapper.readTree(outputFile);
					Iterator<String> iterator=jsonNode.fieldNames();
					
					while(iterator.hasNext()){
						String fieldName=iterator.next();
						if(fieldName.endsWith("DBInstanceIdentifier")){
							JsonNode dbInstanceIdNode=jsonNode.get(fieldName);
							awsHost=dbInstanceIdNode.asText();
							break;
						}
					}
					
				}
			}
			
			String instance = awsHost;
			String schema = table.getAwsSchema();
			String tableId = table.getAwsTableName();
			File ddlFile = new File(file.getParentFile(), tableDefinition.getQuery());
			String createQuery = fileToString(ddlFile);
			
			connection = MySqlConnection.getConnection(instance, schema);			
			statement = connection.createStatement();
			statement.execute(createQuery);
			deployment.setResponse("Created Table " + tableId);
			
		} catch (IOException | SQLException ex ) {
			throw ex;
		}finally {
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
		return deployment;
	}
	
	private String fileToString(File ddlFile) throws IOException {
		return new String(Files.readAllBytes(ddlFile.toPath()));
	}
}

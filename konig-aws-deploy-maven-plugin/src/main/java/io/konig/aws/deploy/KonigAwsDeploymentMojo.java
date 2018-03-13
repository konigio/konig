package io.konig.aws.deploy;

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


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.aws.datasource.AwsAuroraDefinition;
import io.konig.aws.datasource.AwsAuroraTableReference;
import io.konig.maven.MySqlConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


@Mojo( name = "awsDeploy")
public class KonigAwsDeploymentMojo extends AbstractMojo {
   
	@Parameter(required=true)
	private File directory;

	@Parameter(required=true)
	private File tables;
	
	private Connection connection = null;
	private Statement statement = null;
	
	public void execute() throws MojoExecutionException {
		try {
			
			File[] tableFiles = tables.listFiles();
			for (File file : tableFiles) {
				createTables(file);
			}
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to connect the database ", e);
		} finally {
			close();
		}
	}

	
	private void createTables(File file) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		AwsAuroraDefinition tableDefinition = mapper.readValue(file, AwsAuroraDefinition.class);
		AwsAuroraTableReference table = tableDefinition.getTableReference();
		String awsHost = table.getAwsAuroraHost();
		
		if(System.getProperty(awsHost) != null) {
			awsHost = System.getProperty(awsHost);
		}
		
		String instance = awsHost;
		String schema = table.getAwsSchema();
		File ddlFile = new File(file.getParentFile(), tableDefinition.getQuery());
		String createQuery = fileToString(ddlFile);
		
		connection = MySqlConnection.getConnection(instance, schema);			
		statement = connection.createStatement();
		statement.execute(createQuery);
		close();
	}
	
	private void close() throws MojoExecutionException {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				throw new MojoExecutionException("Failed to close the connection ", e);
			}
		}
		
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new MojoExecutionException("Failed to close the connection ", e);
			}
		}
	}
	
	private String fileToString(File ddlFile) throws IOException {
		return new String(Files.readAllBytes(ddlFile.toPath()));
	}
}

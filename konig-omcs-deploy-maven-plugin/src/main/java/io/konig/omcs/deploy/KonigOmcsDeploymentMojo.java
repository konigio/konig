package io.konig.omcs.deploy;


/*
 * #%L
 * konig-omcs-deploy-maven-plugin Maven Plugin
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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
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
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.maven.OmcsConnection;
import io.konig.omcs.datasource.OracleTableDefinition;

@Mojo(name = "omcsDeploy")
public class KonigOmcsDeploymentMojo extends AbstractMojo {

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
		OracleTableDefinition table = mapper.readValue(file, OracleTableDefinition.class);
		String oracleHost = table.getTableReference().getOracleHost();
		if(System.getProperty(oracleHost) != null) {
			oracleHost = System.getProperty(oracleHost);
		}
		String instance = oracleHost +":" +table.getTableReference().getOmcsInstanceId();
		String schema = table.getTableReference().getOracleSchema();
		String createQuery = table.getQuery();
		connection = OmcsConnection.getConnection(instance, schema);			
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
}

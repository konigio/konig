package io.konig.maven;

import java.io.File;

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


import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.konig.omcs.datasource.OracleTableDefinition;

public class CreateOmcsTableAction {
	
	private OmcsDeployment ocmsDeployment;
	
	public CreateOmcsTableAction(OmcsDeployment ocmsDeployment) {
		this.ocmsDeployment = ocmsDeployment;
	}
	
	public OmcsDeployment from(String tableFile) throws Exception {
		Connection connection = null;
		Statement statement = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			File file = ocmsDeployment.file(tableFile);
			OracleTableDefinition table = mapper.readValue(file, OracleTableDefinition.class);
			String oracleHost = table.getTableReference().getOracleHost();
			if(System.getProperty(oracleHost) != null) {
				oracleHost = System.getProperty(oracleHost);
			}
			String instance = oracleHost +":" +table.getTableReference().getOmcsInstanceId();
			String schema = table.getTableReference().getOracleSchema();
			String tableId = table.getTableReference().getOmcsTableId();
			File ddlFile = new File(file.getParentFile(), table.getQuery());
			String createQuery = fileToString(ddlFile);
			
			connection = OmcsConnection.getConnection(instance, schema);			
			statement = connection.createStatement();
			statement.execute(createQuery);
			ocmsDeployment.setResponse("Created Table " + tableId);
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
		return ocmsDeployment;
	}

	private String fileToString(File ddlFile) throws IOException {
		return new String(Files.readAllBytes(ddlFile.toPath()));
	}
}

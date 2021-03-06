package io.konig.maven;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.model.Database;
import com.google.api.services.sqladmin.model.DatabaseInstance;
import com.google.api.services.sqladmin.model.Operation;
import com.google.api.services.sqladmin.model.User;
import com.google.api.services.sqladmin.model.UsersListResponse;

import io.konig.gcp.common.CloudSqlTable;
import io.konig.gcp.common.GoogleCloudSQLCredentialsNotFoundException;
import io.konig.gcp.common.GoogleCloudService;
import io.konig.gcp.common.GoogleCredentialsNotFoundException;
import io.konig.gcp.common.ReplaceStringReader;

public class DeleteGoogleCloudSqlTableAction {

	private KonigDeployment deployment;

	public DeleteGoogleCloudSqlTableAction(KonigDeployment deployment) {
		this.deployment = deployment;
	}
	
	public KonigDeployment from(String path) throws Exception {
		
		GoogleCloudService service = deployment.getService();
		
		File file = deployment.file(path);		
		CloudSqlTable tableInfo=service.readCloudSqlTableInfo(file);
		
		//Check whether instance and database are available.
		SQLAdmin sqlAdmin = service.sqlAdmin();
		DatabaseInstance instance = service.getDatabaseInstance(tableInfo.getInstance());
		if(instance==null){
			deployment.setResponse("DeleteTable :: Instance "+tableInfo.getInstance()+" not available");
			return deployment;
		}
		Database db = service.getDatabase(tableInfo.getDatabase(),tableInfo.getInstance());				
		if (db == null ){
			deployment.setResponse("DeleteTable :: Database "+tableInfo.getDatabase()+" not available");
			return deployment;
		}
		//1. Get user info from system properties /environment variables
		String userName = System.getProperty("konig.gcp.cloudsql."+tableInfo.getInstance()+".username");
		String password = System.getProperty("konig.gcp.cloudsql."+tableInfo.getInstance()+".password");
		if (userName == null || password == null) {
			userName = System.getenv("KONIG_GCP_CLOUDSQL_"+tableInfo.getInstance()+"_USERNAME");
			password = System.getenv("KONIG_GCP_CLOUDSQL_"+tableInfo.getInstance()+"_PASSWORD");
		}
		if (userName == null || password == null) {
			throw new GoogleCloudSQLCredentialsNotFoundException();
		}
		//2. Get the SQL content from .sql file
		String instanceFilePath=file.getParentFile().getParent()+"\\instances\\"+tableInfo.getInstanceFile();
		DatabaseInstance instanceInfo =service.readDatabaseInstanceInfo(new File(instanceFilePath));
		
		//3. Get MYSQL connection
		service.getMySQLConnection(userName, password, tableInfo,instanceInfo);
		
		//4. Create the table
		if(service.isTablePresent(tableInfo)){
			service.deleteTable(tableInfo.getName());
			deployment.setResponse("Deleted  Table " + tableInfo.getName());
		}
		else{
			deployment.setResponse("DeleteTable :: Table " + tableInfo.getName()+" is not already available");
		}
		
		return deployment;
		
		
	}



}

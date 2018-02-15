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
import java.io.IOException;

import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.SQLAdmin.Databases.Insert;
import com.google.api.services.sqladmin.model.Database;
import com.google.api.services.sqladmin.model.DatabaseInstance;
import com.google.api.services.sqladmin.model.Operation;

import io.konig.gcp.common.GoogleCloudService;

public class DeleteGoogleCloudSqlDatabaseAction {

	private KonigDeployment deployment;

	public DeleteGoogleCloudSqlDatabaseAction(KonigDeployment deployment) {
		this.deployment = deployment;
	}
	
	public KonigDeployment from(String path) throws IOException {
		GoogleCloudService service = deployment.getService();
		File file = deployment.file(path);
		try {
			SQLAdmin sqlAdmin = service.sqlAdmin();
			Database info = service.readDatabaseInfo(file);
				DatabaseInstance instance = service.getDatabaseInstance(info.getInstance());
				if(instance==null){
					deployment.setResponse("DeleteDatabase :: Instance "+info.getInstance()+" not available");
					return deployment;
				}
				Database db = service.getDatabase(info.getName(),info.getInstance());				
				if (db != null ){
					Operation operation=service.sqlAdmin().databases().delete(service.getProjectId(), info.getInstance(), info.getName()).execute();
					deployment.setResponse("Deleted  Database " + info.getName());
				}
				else{
					deployment.setResponse("DeleteDatabase :: Database "+info.getName()+" is not available");
				}
			
		} catch (Exception ex) {
			throw ex;
		}
		return deployment;
	}

}

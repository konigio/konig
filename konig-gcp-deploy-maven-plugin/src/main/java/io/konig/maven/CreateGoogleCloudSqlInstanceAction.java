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
import com.google.api.services.sqladmin.model.DatabaseInstance;
import com.google.api.services.sqladmin.model.Operation;
import com.google.api.services.sqladmin.model.Settings;

import io.konig.gcp.common.GoogleCloudService;

public class CreateGoogleCloudSqlInstanceAction {

	private KonigDeployment deployment;

	public CreateGoogleCloudSqlInstanceAction(KonigDeployment deployment) {
		this.deployment = deployment;
	}
	
	public KonigDeployment from(String path) throws IOException {
		GoogleCloudService service = deployment.getService();
		File file = deployment.file(path);
		String status=null;
		try {
			DatabaseInstance info = service.readDatabaseInstanceInfo(file);
			SQLAdmin sqlAdmin=service.sqlAdmin();
			DatabaseInstance instance = service.getDatabaseInstance(info.getName());
			if (instance == null) {	
			Operation operation=service.sqlAdmin().instances().insert(service.getProjectId(), info).execute();
			System.out.println("Waiting for instance to be created :: " + info.getName()+"\n");
			//TODO: Tune the below code
			long startTime = System.currentTimeMillis();
			if("PENDING".equals(operation.getStatus())){
				while((System.currentTimeMillis()-startTime)<600000)
				{
					if((System.currentTimeMillis()-startTime)>300000){
						DatabaseInstance instance1=service.sqlAdmin().instances().get(service.getProjectId(), info.getName()).execute();
						if("RUNNABLE".equals(instance1.getState())){
							break;
						}
					}
				}
			}
			deployment.setResponse("Created  Instance " + info.getName());
			}
		} catch (Exception ex) {
			throw ex;
		}
		return deployment;
	}

}

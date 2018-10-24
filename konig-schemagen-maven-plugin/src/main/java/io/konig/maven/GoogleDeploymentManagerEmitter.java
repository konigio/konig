package io.konig.maven;

/*
 * #%L
 * Konig Schema Generator Maven Plugin
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

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.io.Emitter;
import io.konig.gcp.deployment.GcpDeploymentConfigManager;
import io.konig.schemagen.gcp.CloudSqlAdminManager;

public class GoogleDeploymentManagerEmitter implements Emitter {
	
	private CloudSqlAdminManager adminManager;
	private GcpDeploymentConfigManager deployManager;
	private File deploymentYaml;
	
	

	public GoogleDeploymentManagerEmitter(CloudSqlAdminManager adminManager,
			GcpDeploymentConfigManager deployManager, File deploymentYaml) {
		this.adminManager = adminManager;
		this.deployManager = deployManager;
		this.deploymentYaml = deploymentYaml;
	}


	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		adminManager.load(graph);
		deployManager.writeConfig(deploymentYaml);

	}

}

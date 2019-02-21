package io.konig.gcp.deployment;

/*
 * #%L
 * Konig GCP Deployment Manager
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.io.Emitter;
import io.konig.shacl.ShapeManager;

public class DeploymentConfigEmitter implements Emitter {
	
	private ShapeManager shapeManager;
	private File configFile;


	public DeploymentConfigEmitter(ShapeManager shapeManager, File configFile) {
		this.shapeManager = shapeManager;
		this.configFile = configFile;
	}


	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		
		configFile.getParentFile().mkdirs();
		GcpConfigManager manager = new GcpConfigManager();
		manager.build(graph, shapeManager);
		try {
			manager.write(configFile);
		} catch (Exception e) {
			throw new KonigException(e);
		}
		
	}

}

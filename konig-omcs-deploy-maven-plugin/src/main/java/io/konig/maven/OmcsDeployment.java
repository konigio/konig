package io.konig.maven;

import java.io.BufferedReader;

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
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

public class OmcsDeployment {
	
	private File baseDir;
	private String response;
	
	public OmcsDeployment(String baseDir) throws Exception {
		this.baseDir =  new File(baseDir).getAbsoluteFile();
	}

	public Object create(OmcsResourceType type) throws SQLException {
			switch (type) {
			case OracleDatabase:
				return new CreateOmcsDatabaseAction(this);
	
			case OracleTable:
				return new CreateOmcsTableAction(this);
	
			default:
				break;
	
			}
		
		return null;
	}
	
	public void setResponse(String response) {
		this.response = response;
	}
	public File file(String path) {
		return new File(baseDir, path);
	}
	public String getResponse() {
		return this.response;
	}
}

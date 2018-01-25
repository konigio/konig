package io.konig.omcs.datasource;

/*
 * #%L
 * Konig Oracle Managed Cloud Model
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


public class OracleDatabaseRefence {
	private String omcsInstanceId;
	private String omcsDatabaseId;
	
	public OracleDatabaseRefence() {
		
	}
	
	public String getOmcsInstanceId() {
		return omcsInstanceId;
	}
	public String getOmcsDatabaseId() {
		return omcsDatabaseId;
	}

	public void setOmcsInstanceId(String omcsInstanceId) {
		this.omcsInstanceId = omcsInstanceId;
	}
	public void setOmcsDatabaseId(String omcsDatabaseId) {
		this.omcsDatabaseId = omcsDatabaseId;
	}
	
	public OracleDatabaseRefence(String omcsInstanceId, String omcsDatabaseId) {
		this.omcsInstanceId = omcsInstanceId;
		this.omcsDatabaseId = omcsDatabaseId;
	}
}

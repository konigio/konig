package io.konig.openapi.model;

/*
 * #%L
 * Konig OpenAPI model
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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


import java.util.HashMap;
import java.util.Map;

import io.konig.yaml.YamlMap;
import io.konig.yaml.YamlProperty;

public class Server {

	private String url;
	private String description;
	private Map<String, ServerVariable> variableMap = new HashMap<>();
	

	public Server() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@YamlMap("variables")
	public void addVariable(ServerVariable var) {
		variableMap.put(var.getName(), var);
	}
	
	public ServerVariable getVariable(String varName) {
		return variableMap.get(varName);
	}
	
	

}

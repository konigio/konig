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


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.konig.yaml.Yaml;
import io.konig.yaml.YamlMap;
import io.konig.yaml.YamlProperty;

public class OpenAPI {

	private String openapi;
	private Info info;
	private List<Server> serverList = new ArrayList<>();
	private PathMap pathMap = new PathMap();
	private Components components;
	
	public String getOpenapi() {
		return openapi;
	}
	public void setOpenapi(String openapi) {
		this.openapi = openapi;
	}
	public Info getInfo() {
		return info;
	}
	public void setInfo(Info info) {
		this.info = info;
	}
	
	@YamlProperty("servers")
	public void addServer(Server server) {
		serverList.add(server);
	}
	public List<Server> getServerList() {
		return serverList;
	}

	@YamlMap("paths")
	public void addPath(Path path) {
		pathMap.put(path.stringValue(), path);
	}
	
	public PathMap getPaths() {
		return pathMap;
	}
	
	public Collection<Path> listPaths() {
		return pathMap.values();
	}
	
	public Path getPath(String stringValue) {
		return pathMap.get(stringValue);
	}
	public Components getComponents() {
		return components;
	}
	
	public void setComponents(Components components) {
		this.components = components;
	}
	
	public String toString() {
		return Yaml.toString(this);
	}

}

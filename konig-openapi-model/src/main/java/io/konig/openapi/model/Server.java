package io.konig.openapi.model;

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

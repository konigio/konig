package io.konig.openapi.model;

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

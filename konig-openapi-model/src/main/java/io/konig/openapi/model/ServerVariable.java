package io.konig.openapi.model;

import java.util.ArrayList;
import java.util.List;

public class ServerVariable {

	private String name;
	private String defaultValue;
	private String description;
	private List<String> enumValue;
	
	public ServerVariable(String name) {
		this.name=name;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDefault() {
		return defaultValue;
	}
	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void addEnum(String value) {
		if (enumValue == null) {
			enumValue = new ArrayList<>();
		}
		enumValue.add(value);
	}
	
	public List<String> getEnum() {
		return enumValue;
	}	
	
	

}

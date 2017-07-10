package io.konig.openapi.model;

import java.util.HashMap;
import java.util.Map;

public class RequestBody {

	private String description;
	private Map<String, MediaType> mediaTypeMap = new HashMap<>();
	private boolean required;
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}
	
	public Map<String, MediaType> getContent() {
		return mediaTypeMap;
	}
	
	

}

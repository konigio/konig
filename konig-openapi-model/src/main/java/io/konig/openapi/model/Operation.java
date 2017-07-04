package io.konig.openapi.model;

import java.util.ArrayList;
import java.util.List;

import io.konig.yaml.YamlProperty;

public class Operation {

	private String operationId;
	private String summary;
	private String description;
	private ExternalDocumentation externalDocs;
	private List<String> tags = new ArrayList<>();
	private List<Parameter> parameterList = new ArrayList<>();
	private ResponseMap responses;
	
	@YamlProperty("tags")
	public void addTag(String tag) {
		tags.add(tag);
	}
	
	public List<String> getTags() {
		return tags;
	}
	

	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ExternalDocumentation getExternalDocs() {
		return externalDocs;
	}

	public void setExternalDocs(ExternalDocumentation externalDocs) {
		this.externalDocs = externalDocs;
	}
	
	@YamlProperty("parameters")
	public void addParameter(Parameter p) {
		parameterList.add(p);
	}

	public List<Parameter> getParameterList() {
		return parameterList;
	}

	public ResponseMap getResponses() {
		return responses;
	}

	public void setResponses(ResponseMap responses) {
		this.responses = responses;
	}
	
	

}

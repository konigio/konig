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

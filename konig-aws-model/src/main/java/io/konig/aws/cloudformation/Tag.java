package io.konig.aws.cloudformation;

/*
 * #%L
 * Konig AWS Model
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


import com.fasterxml.jackson.annotation.JsonProperty;

public class Tag {
	
	@JsonProperty("Key")
	private String tagKey;
	
	@JsonProperty("Value")
	private String tagValue;
	
	@JsonProperty("Environment")
	private String environment;
	
	public Tag() {
		
	}
	public Tag(String tagKey, String tagValue) {
		this.tagKey = tagKey;
		this.tagValue = tagValue;
	}
	
	public String getTagKey() {
		return tagKey;
	}
	public String getTagValue() {
		return tagValue;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setTagKey(String tagKey) {
		this.tagKey = tagKey;
	}
	public void setTagValue(String tagValue) {
		this.tagValue = tagValue;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	
}

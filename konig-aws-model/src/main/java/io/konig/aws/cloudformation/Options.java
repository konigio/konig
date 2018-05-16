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

public class Options {
	
	@JsonProperty("awslogs-group")
	private String awslogsGroup;
	
	@JsonProperty("awslogs-region")
	private String awslogRegion;
	
	@JsonProperty("awslogs-stream-prefix")
	private String awslogsStreamPrefix;

	public String getAwslogsGroup() {
		return awslogsGroup;
	}

	public String getAwslogRegion() {
		return awslogRegion;
	}

	public String getAwslogsStreamPrefix() {
		return awslogsStreamPrefix;
	}

	public void setAwslogsGroup(String awslogsGroup) {
		this.awslogsGroup = awslogsGroup;
	}

	public void setAwslogRegion(String awslogRegion) {
		this.awslogRegion = awslogRegion;
	}

	public void setAwslogsStreamPrefix(String awslogsStreamPrefix) {
		this.awslogsStreamPrefix = awslogsStreamPrefix;
	}
}
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

public class LoadBalancer {
	
	@JsonProperty("ContainerName")
	private String containerName;
	
	@JsonProperty("ContainerPort")
	private String containerPort;
	
	@JsonProperty("TargetGroupArn")
	private String targetGroupArn;

	public String getContainerName() {
		return containerName;
	}

	public String getContainerPort() {
		return containerPort;
	}

	public String getTargetGroupArn() {
		return targetGroupArn;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public void setContainerPort(String containerPort) {
		this.containerPort = containerPort;
	}

	public void setTargetGroupArn(String targetGroupArn) {
		this.targetGroupArn = targetGroupArn;
	}
	
}

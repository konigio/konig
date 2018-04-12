package io.konig.aws.cloudformation;

import java.util.List;

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

public class ContainerDefinition {
	
	@JsonProperty("Name")
	private String name;
	
	@JsonProperty("Image")
	private String image;
	
	@JsonProperty("MemoryReservation")
	private String memoryReservation;
	
	@JsonProperty("PortMappings")
	private List<PortMapping> portMappings;

	public String getName() {
		return name;
	}

	public String getImage() {
		return image;
	}

	public String getMemoryReservation() {
		return memoryReservation;
	}

	public List<PortMapping> getPortMappings() {
		return portMappings;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setMemoryReservation(String memoryReservation) {
		this.memoryReservation = memoryReservation;
	}

	public void setPortMappings(List<PortMapping> portMappings) {
		this.portMappings = portMappings;
	}
	
}

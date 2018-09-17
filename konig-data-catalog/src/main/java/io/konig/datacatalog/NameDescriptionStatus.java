package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


public class NameDescriptionStatus implements Comparable<NameDescriptionStatus> {
	private Link name;
	private String description;
	private Link status;
	
	public NameDescriptionStatus(Link name, String description, Link status) {
		this.name = name;
		this.description = description;
		this.status = status;
	}
	
	public Link getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public Link getStatus() {
		return status;
	}

	@Override
	public int compareTo(NameDescriptionStatus o) {
		return this.name.getName().compareTo(o.getName().getName());
	}
	
	
	

}

package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


public class ResourceDescription {

	private String href;
	private String name;
	private String description;
	
	public ResourceDescription(String href, String name, String description) {
		this.href = href;
		this.name = name;
		this.description = description==null ? "" : description;
	}

	public String getHref() {
		return href;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	

}

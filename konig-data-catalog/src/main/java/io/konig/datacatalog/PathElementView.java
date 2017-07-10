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


import java.util.List;

public class PathElementView extends Link {
	private String operator;
	private List<PropertyValuePair> filter;
	
	public PathElementView(List<PropertyValuePair> filter) {
		super(null, null);
		this.filter = filter;
	}
	
	public PathElementView(String operator, String name, String href) {
		super(name, href);
		this.operator = operator;
	}

	public String getOperator() {
		return operator;
	}

	public List<PropertyValuePair> getFilter() {
		return filter;
	}

}

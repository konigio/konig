package io.konig.data.app.common;

import java.util.HashMap;
import java.util.Map;

/*
 * #%L
 * Konig DAO Core
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


import org.openrdf.model.URI;

import io.konig.dao.core.Format;

public class GetRequest {

	private URI individualId;
	private Format format;
	private URI shapeId;
	private Map<String,String> queryParams;

	public URI getIndividualId() {
		return individualId;
	}

	public void setIndividualId(URI individualId) {
		this.individualId = individualId;
	}

	public Format getFormat() {
		return format;
	}

	public void setFormat(Format format) {
		this.format = format;
	}

	public URI getShapeId() {
		return shapeId;
	}

	public void setShapeId(URI shapeId) {
		this.shapeId = shapeId;
	}
	public Map<String,String> getQueryParams() {
		return queryParams;
	}

	public void setQueryParams(Map<String,String> queryParams) {
		this.queryParams = queryParams; 
	}
}

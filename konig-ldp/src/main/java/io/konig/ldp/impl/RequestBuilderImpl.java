package io.konig.ldp.impl;

/*
 * #%L
 * Konig Linked Data Platform
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import java.util.HashMap;
import java.util.Map;

import io.konig.ldp.AcceptList;
import io.konig.ldp.AcceptableMediaType;
import io.konig.ldp.HttpMethod;
import io.konig.ldp.LdpRequest;
import io.konig.ldp.MediaType;
import io.konig.ldp.RequestBuilder;
import io.konig.ldp.ResourceFile;

public class RequestBuilderImpl implements RequestBuilder {

	private HttpMethod method;
	private String resourceId;
	private AcceptList acceptList = new AcceptList();
	private byte[] body;
	private Map<String, String> headerMap = new HashMap<>();

	@Override
	public RequestBuilder header(String key, String value) {
		if (value != null) {
			headerMap.put(key, value);
		}
		return this;
	}

	@Override
	public RequestBuilder method(HttpMethod method) {
		this.method = method;
		return this;
	}

	@Override
	public RequestBuilder body(byte[] body) {
		this.body = body;
		return this;
	}

	@Override
	public LdpRequest build() {
		return new LdpRequestImpl(method, resourceId, acceptList, body, headerMap);
	}

	@Override
	public RequestBuilder address(String resourceId) {
		this.resourceId = resourceId;
		return this;
	}

	@Override
	public RequestBuilder slug(String slugValue) {
		return header("Slug", slugValue);
	}

	@Override
	public RequestBuilder accept(String contentType) {
		acceptList.parse(contentType);
		
		return this;
	}

	@Override
	public RequestBuilder entity(ResourceFile resource) {
		address(resource.getContentLocation());
		header("Content-Type", resource.getContentLocation());
		return this;
	}

	@Override
	public RequestBuilder body(String body) {
		return body(body.getBytes());
	}

	@Override
	public RequestBuilder contentType(String type) {
		return header("Content-Type", type);
	}

}

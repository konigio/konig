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


import java.util.Map;

import io.konig.ldp.AcceptList;
import io.konig.ldp.HttpMethod;
import io.konig.ldp.LdpRequest;
import io.konig.ldp.RdfSource;
import io.konig.ldp.ResourceFile;
import io.konig.ldp.ResourceType;

public class LdpRequestImpl implements LdpRequest {
	private HttpMethod method;
	private String resourceId;
	private AcceptList acceptList;
	private byte[] body;
	private Map<String, String> headerMap;
	
	

	public LdpRequestImpl(HttpMethod method, String resourceId, AcceptList acceptList, byte[] body,
			Map<String, String> headerMap) {
		this.method = method;
		this.resourceId = resourceId;
		this.acceptList = acceptList;
		this.body = body;
		this.headerMap = headerMap;
	}

	@Override
	public HttpMethod getMethod() {
		return method;
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}

	@Override
	public AcceptList getAcceptList() {
		return acceptList;
	}

	@Override
	public byte[] getEntityBody() {
		return body;
	}

	@Override
	public Iterable<String> getHeaderNames() {
		return headerMap.keySet();
	}

	@Override
	public String getHeader(String headerName) {
		return headerMap.get(headerName);
	}

	@Override
	public String getSlug() {
		return getHeader("Slug");
	}

	@Override
	public String getContentType() {
		return getHeader("Content-Type");
	}

	@Override
	public void setContentType(String contentType) {
		headerMap.put("Content-Type", contentType);
		
	}

	@Override
	public RdfSource asRdfSource() {
		return new RdfSourceImpl(getResourceId(), getContentType(), ResourceType.RDFSource, getEntityBody());
	}


}

package io.konig.ldp.client;

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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import io.konig.core.IriRewriter;
import io.konig.ldp.AcceptList;
import io.konig.ldp.HttpMethod;
import io.konig.ldp.HttpStatusCode;
import io.konig.ldp.LdpException;
import io.konig.ldp.LdpHeader;
import io.konig.ldp.LdpRequest;
import io.konig.ldp.LdpResponse;
import io.konig.ldp.LinkedDataPlatform;
import io.konig.ldp.RequestBuilder;
import io.konig.ldp.ResourceBuilder;
import io.konig.ldp.ResourceFile;
import io.konig.ldp.ResourceType;
import io.konig.ldp.impl.LDPUtil;
import io.konig.ldp.impl.LdpResponseImpl;
import io.konig.ldp.impl.MemoryLdpHeader;
import io.konig.ldp.impl.RequestBuilderImpl;
import io.konig.ldp.impl.SimpleResourceBuilder;

public class LdpClient implements LinkedDataPlatform {
	
	private IriRewriter rewriter;

	public LdpClient() {
	}

	public LdpClient(IriRewriter rewriter) {
		this.rewriter = rewriter;
	}

	@Override
	public ResourceBuilder getResourceBuilder() {
		return new SimpleResourceBuilder();
	}

	@Override
	public RequestBuilder getRequestBuilder() {
		return new RequestBuilderImpl();
	}

	@Override
	public LdpResponse createResponse(OutputStream out) {
		return new LdpResponseImpl(out);
	}

	@Override
	public LdpResponse createResponse() {
		return new LdpResponseImpl(null);
	}

	@Override
	public void post(String containerId, ResourceFile resource) throws IOException, LdpException {
		
		// TODO: Infer slug from resource.contentLocation.
		
		LdpRequest request = getRequestBuilder()
			.method(HttpMethod.POST)
			.address(containerId)
			.contentType(resource.getContentType())
			.body(resource.getEntityBody())
			.build();
		
		LdpResponse response = createResponse();
		
		serve(request, response);
		
	}

	@Override
	public ResourceFile get(String resourceIRI) throws IOException, LdpException {
		resourceIRI = rewrite(resourceIRI);
		
		LdpRequest request = getRequestBuilder()
				.method(HttpMethod.GET)
				.address(resourceIRI)
				.build();
			
		LdpResponse response = createResponse();
		
		serve(request, response);
		
		return response.getResource();
	}

	private String rewrite(String resourceIRI) {
		return rewriter==null ? resourceIRI : rewriter.rewrite(resourceIRI);
	}

	@Override
	public int put(ResourceFile resource) throws IOException, LdpException {
		
		LdpRequest request = getRequestBuilder()
				.method(HttpMethod.PUT)
				.address(resource.getContentLocation())
				.contentType(resource.getContentType())
				.body(resource.getEntityBody())
				.build();
			
		LdpResponse response = createResponse();
		
		return serve(request, response);
	}

	@Override
	public void delete(String resourceIRI) throws IOException, LdpException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int serve(LdpRequest request, LdpResponse response) throws IOException, LdpException {
		LdpHeader ldpHeader = new MemoryLdpHeader();
		response.setHeader(ldpHeader);
		String resourceId = request.getResourceId();
		String contentType = request.getContentType();
		AcceptList acceptList = request.getAcceptList();
		byte[] body = request.getEntityBody();
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		HttpUriRequest httpRequest = null;
		HttpEntityEnclosingRequestBase entityRequest = null;
		
		switch (request.getMethod()) {
		
		case POST :
			httpRequest = entityRequest = new HttpPost(resourceId);
			String slug = request.getSlug();
			if (slug != null) {
				httpRequest.setHeader("Slug", slug);
			}
			break;
			
		case PUT :
			httpRequest = entityRequest = new HttpPut(resourceId);
			break;
			
		case GET :
			httpRequest = new HttpGet(resourceId);
			break;
			
		default:
		}
		
		if (acceptList != null) {
			httpRequest.setHeader("Accept", acceptList.toString());
		}
		
		if (entityRequest != null) {

			if (contentType == null) {
				throw new LdpException("Content-Type must be defined", HttpStatusCode.BAD_REQUEST);
			}
			
			if (body == null) {
				throw new LdpException("Entity body must be defined", HttpStatusCode.BAD_REQUEST);
			}
			ByteArrayEntity entity = new ByteArrayEntity(body);
			entity.setContentType(contentType);
			entityRequest.setEntity(entity);
		}
		
		CloseableHttpResponse httpResponse = client.execute(httpRequest);
		
		copyHeaders(httpResponse, ldpHeader);
		
		createResponseEntity(httpResponse, response);
		
		return httpResponse.getStatusLine().getStatusCode();
		
	}


	private void createResponseEntity(CloseableHttpResponse httpResponse, LdpResponse response) throws IllegalStateException, IOException {
		
		
		HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			
			Header header = httpResponse.getFirstHeader("Content-Type");
			String contentType = header==null ? null : header.getValue();
			
			header = httpResponse.getFirstHeader("Content-Location");
			if (header == null) {
				header = httpResponse.getFirstHeader("Location");
			}
			String contentLocation = header==null ? null : header.getValue();
			
			byte[] buffer = new byte[1024];
			int len;
			
			@SuppressWarnings("resource")
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream input = entity.getContent();
			while ( (len=input.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			input.close();
			
			ResourceType type = getResourceType(httpResponse);
			
			ResourceFile resource = getResourceBuilder()
				.type(type)
				.entityBody(out.toByteArray())
				.contentLocation(contentLocation)	
				.contentType(contentType)
				.resource();
			
			response.setResource(resource);
		}
	}

	private ResourceType getResourceType(CloseableHttpResponse httpResponse) {
		
		ResourceType result = ResourceType.Resource;
		Header[] list = httpResponse.getHeaders("Link");
		if (list != null) {
			for (int i=0; i<list.length; i++) {
				String value = list[i].getValue();
				ResourceType type = LDPUtil.getResourceType(value);
				if (type != null && type.isSubClassOf(result)) {
					result = type;
				}
			}
		}
		
		
		return result;
	}

	private void copyHeaders(HttpResponse httpResponse, LdpHeader ldpHeader) {
		
		Header[] list = httpResponse.getAllHeaders();
		for (int i=0; i<list.length; i++) {
			Header header = list[i];
			ldpHeader.put(header.getName(), header.getValue());
		}
		
	}


}

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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.vocab.LDP;
import io.konig.ldp.AcceptList;
import io.konig.ldp.AcceptableMediaType;
import io.konig.ldp.BasicContainer;
import io.konig.ldp.Container;
import io.konig.ldp.HttpStatusCode;
import io.konig.ldp.LdpException;
import io.konig.ldp.LdpHeader;
import io.konig.ldp.LdpRequest;
import io.konig.ldp.LdpResponse;
import io.konig.ldp.LdpWriter;
import io.konig.ldp.LinkedDataPlatform;
import io.konig.ldp.MediaType;
import io.konig.ldp.RdfSource;
import io.konig.ldp.RequestBuilder;
import io.konig.ldp.ResourceFile;
import io.konig.ldp.ResourceType;

public abstract class AbstractPlatform implements LinkedDataPlatform {
	
	private String root;
	private LdpWriter ldpWriter;
	
	

	public AbstractPlatform(String root) {
		this.root = root;
		ldpWriter = new GenericLdpWriter();
	}

	
	public String getBaseURL() {
		return root;
	}
	

	@Override
	public void post(String containerId, ResourceFile resource) throws IOException, LdpException {
		
		ResourceFile target = get(containerId);
		if (target == null) {
			target = getResourceBuilder()
				.contentLocation(containerId)
				.basicContainer();
			
			save(target);
			put(target, false);
			getParentContainer(target.getContentLocation());
		}
		
		if (!target.isContainer()) {
			throw new LdpException("Resource is not a container: " + containerId, HttpStatusCode.BAD_REQUEST);
		}
		Container container = target.asContainer();
		save(resource);
		container.add(resource);
	}

	private Container getParentContainer(String resourceId) throws IOException, LdpException {
		String containerId = parentId(resourceId);
		if (containerId.length() < root.length()) {
			return null;
		}
		
		ResourceFile container =  get(containerId);
		if (container == null) {
			container = getResourceBuilder()
				.contentLocation(containerId)
				.contentType("text/turtle")
				.basicContainer();
			
			save(container);
			
			put(container, true);
		}
		return container.asBasicContainer();
	}

	private String parentId(String resourceId) {
		int end = resourceId.length()-1;
		if (resourceId.charAt(end)=='/') {
			end--;
		}
		int slash = resourceId.lastIndexOf('/', end);
		return resourceId.substring(0, slash+1);
	}

	@Override
	public int put(ResourceFile resource) throws IOException, LdpException {
		return put(resource, true);
	}
	
	protected int put(ResourceFile resource, boolean createContainer) throws IOException, LdpException {
		
		String contentType = resource.getContentType();
		if (contentType == null) {
			throw new LdpException("Content-Type must be defined", HttpStatusCode.BAD_REQUEST);
		}

		
		Container container = null;
		if (createContainer) {
			container = getParentContainer(resource.getContentLocation());
		}
		int result = save(resource);
		if (container != null) {
			container.add(resource);
		}
		return result;
	}

	protected abstract int save(ResourceFile resource) throws IOException;


	@Override
	public void delete(String resourceIRI) throws IOException, LdpException {
		doDelete(resourceIRI);
		
		Container container = getParentContainer(resourceIRI);
		if (container != null) {
			container.remove(resourceIRI);
		}
		
	}

	protected abstract void doDelete(String resourceIRI) throws IOException, LdpException;

	@Override
	public int serve(LdpRequest request, LdpResponse response) throws IOException, LdpException {
		int result = HttpStatusCode.OK;
		if (response.getHeader()==null) {
			response.setHeader(new MemoryLdpHeader());
		}
		if (response.getOutputStream() == null) {
			response.setOutputStream(new ByteArrayOutputStream());
		}
		
		switch (request.getMethod()) {
		case GET :	doGet(request, response); break;
		case POST: doPost(request, response); break;
		case PUT: result=doPut(request, response); break;
		case DELETE:
		default:
			throw new LdpException("Method not supported: " + request.getMethod());
		}
		
		response.flush();
		
		OutputStream out = response.getOutputStream();
		if (out instanceof ByteArrayOutputStream) {
			ByteArrayOutputStream buffer = (ByteArrayOutputStream) out;
			if (buffer.size() > 0) {
				response.getResource().setEntityBody(((ByteArrayOutputStream) out).toByteArray());
			}
		}
		
		return result;
	}

	private int doPut(LdpRequest request, LdpResponse response) throws IOException, LdpException {
		
		RdfSource source = request.asRdfSource();
		return put(source, true);
		// TODO: send headers
		
	}

	protected void doPost(LdpRequest request, LdpResponse response) throws IOException, LdpException {
		String containerId = request.getResourceId();
//		ResourceFile entity = request.re
//		post(containerId, entity);
		
		// TODO: write response
		
	}


	private void doGet(LdpRequest request, LdpResponse response) throws IOException, LdpException {
		
		
		ResourceFile file = get(request.getResourceId());
		response.setResource(file);
		
		if (file.isBasicContainer()) {
			getBasicContainer(request, response);
		}

		setContentType(request, response);
		putLinkHeader(response);
		
		ldpWriter.write(response);
		
	}

	private void setContentType(LdpRequest request, LdpResponse response) throws LdpException {
		
		MediaType selectedType = null;
		String defaultType = response.getResource().getContentType();
		boolean isRdfSource = response.getResource().getType().isSubClassOf(ResourceType.RDFSource);
		AcceptList list = request.getAcceptList();
		if (list==null || list.isEmpty()) {
			selectedType = MediaType.instance(defaultType);
		} else {
			list.sort();
			for (AcceptableMediaType x : list) {
				MediaType mediaType = x.getMediaType();
				if (mediaType.getFullName().equals(defaultType)) {
					selectedType = mediaType;
					break;
				}
				if (isRdfSource && LDPUtil.isRdfSourceMediaType(mediaType.getFullName())) {
					selectedType = mediaType;
					break;
				}
			}
		}
		
		if (selectedType == null) {
			throw new LdpException(
				"Content-Type for the response is not known.  Please set the Accept header in the request.",
				HttpStatusCode.BAD_REQUEST);
		}
		
		response.setTargetMediaType(selectedType);
		LdpHeader header = response.getHeader();
		header.put("Content-Type", selectedType.getFullName());
		
	}

	private void putLinkHeader(LdpResponse response) throws LdpException {
		
		ResourceFile resource = response.getResource();
		ResourceType type = ResourceType.Resource;
		if (resource != null && resource.getType()!=null) {
			type = resource.getType();
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append('<');
		builder.append(type.getURI().stringValue());
		builder.append("> rel=\"type\"");
		
		response.getHeader().put("Link", builder.toString());
		
		
	}

	private void getBasicContainer(LdpRequest request, LdpResponse response) throws LdpException, IOException {
		
		BasicContainer container = response.getResource().asBasicContainer();
		Graph graph = container.createGraph();
		URI subject = uri(container.getContentLocation());
		
		graph.edge(subject, RDF.TYPE, LDP.BasicContainer);
		
		for (String memberId : container.getMemberIds()) {
			URI object = uri(memberId);
			graph.edge(subject, LDP.contains, object);
		}
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
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
	public RequestBuilder getRequestBuilder() {
		return new RequestBuilderImpl();
	}
}

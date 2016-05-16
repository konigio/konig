package io.konig.ldp;

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
import java.io.OutputStream;

public interface LinkedDataPlatform {
	
	ResourceBuilder getResourceBuilder();
	RequestBuilder getRequestBuilder();
	LdpResponse createResponse(OutputStream out);
	LdpResponse createResponse();
	
	void post(String containerId, ResourceFile resource) throws IOException, LdpException;
	ResourceFile get(String resourceIRI) throws IOException, LdpException;
	
	/**
	 * Create or update the specified resource, and optionally create an LDP container to
	 * hold the resource.
	 * @param resource The resource that is being created or updated within the server.
	 * @param createContainer A flag which specifies whether a container should be created to
	 * hold the resource.  If true, the server will construct an IRI for the container by removing
	 * the local name from the supplied resource.  If a container with that IRI does not exist,
	 * the server will construct a Basic Container with the IRI.  Moreover, it will cascade the 
	 * creation of containers until the root container within this server is reached.
	 * @return The status code of the operation, either 200 OK for a successful update, or 
	 * 201 Created if the resource is newly created.
	 * @throws IOException
	 * @throws LdpException
	 */
	int put(ResourceFile resource, boolean createContainer) throws IOException, LdpException;
	
	
	void delete(String resourceIRI) throws IOException, LdpException;
	
	
	void serve(LdpRequest request, LdpResponse response) throws IOException, LdpException;
}

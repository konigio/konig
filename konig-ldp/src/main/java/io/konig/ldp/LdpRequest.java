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


public interface LdpRequest {
	
	HttpMethod getMethod();

	/**
	 * Get the IRI of the resource that was requested.
	 * @return The IRI of the resource that was requested.
	 */
	String getResourceId();
	
	/**
	 * Get the list of acceptable media types
	 * @return The list of acceptable media types
	 */
	AcceptList getAcceptList();
	
	/**
	 * Get the bytes in the entity body, or null if there was no body.
	 * @return The bytes in the entity body, or null if there was no body.
	 */
	byte[] getEntityBody();
	
	/**
	 * Get a list of the names for all HTTP Request headers
	 * @return A list of the names for all HTTP Request headers
	 */
	Iterable<String> getHeaderNames();
	
	/**
	 * Get the value of a given header
	 * @param headerName The name of the header to be returned
	 * @return The header value.
	 */
	String getHeader(String headerName);
	
	/**
	 * Get the Slug header value. This header is defined in RFC5023.  It is used
	 * only in a POST request to suggest the local name for the resource being
	 * posted.
	 * @return The Slug header value.
	 */
	String getSlug();
	
	String getContentType();
	void setContentType(String contentType);
	
	RdfSource asRdfSource();
	
	
}

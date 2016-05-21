package io.konig.appengine.ldp;

/*
 * #%L
 * konig-war
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.konig.ldp.HttpMethod;
import io.konig.ldp.LdpException;
import io.konig.ldp.LdpRequest;
import io.konig.ldp.LdpResponse;
import io.konig.ldp.RequestBuilder;
import io.konig.ldp.ResourceFile;

public class GaeLdpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String BASE_URL = "baseURL";
	
	private GaeLinkedDataPlatform platform;
	
	public void init(ServletConfig config) throws ServletException {
		String baseURL = config.getInitParameter(BASE_URL);
		platform = new GaeLinkedDataPlatform(baseURL);
		
		createRootContainer(baseURL);
	}

	private void createRootContainer(String baseURL) throws ServletException {
		
		try {
			if (platform.get(baseURL) == null) {
				
				ResourceFile resource = platform.getResourceBuilder()
					.contentLocation(baseURL)
					.contentType("text/turtle")
					.body("")
					.basicContainer();
				
				platform.put(resource);
			}
		} catch (IOException | LdpException e) {
			throw new ServletException(e);
		}
		
	}

	protected void doPut(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {
		LdpRequest request = createRequest(HttpMethod.PUT, req);
		LdpResponse response = createResponse(resp);
		try {
			platform.serve(request, response);
		} catch (LdpException e) {
			throw new ServletException(e);
		}
		
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)  throws ServletException, IOException {
		LdpRequest request = createRequest(HttpMethod.GET, req);
		LdpResponse response = createResponse(resp);
		try {
			platform.serve(request, response);
		} catch (LdpException e) {
			throw new ServletException(e);
		}
		
	}
	
	private LdpRequest createRequest(HttpMethod method, HttpServletRequest req) throws IOException {
		RequestBuilder builder = platform.getRequestBuilder()
			.method(method)
			.accept(req.getHeader("Accept"));
		
		switch (method) {
		case PUT :
			
			builder
				.address(getRequestURL(req))
				.contentType(req.getHeader("Content-Type"))
				.body(getEntityBody(req));
			break;
			
		case GET :
			
			builder.address(getRequestURL(req));
			break;
			
		default:
		}
		return builder.build();
	}

	private String getRequestURL(HttpServletRequest req) {
		return req.getRequestURL().toString();
	}

	private byte[] getEntityBody(HttpServletRequest req) throws IOException {
		ServletInputStream input = req.getInputStream();
		ByteArrayOutputStream sink = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len=input.read(buffer)) != -1) {
			sink.write(buffer, 0, len);
		}
		
		return sink.toByteArray();
	}

	private LdpResponse createResponse(HttpServletResponse resp) throws IOException {
		LdpResponse response = platform.createResponse(resp.getOutputStream());
		response.setHeader(new ServletHeader(resp));
		return response;
	}


}

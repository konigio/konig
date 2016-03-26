package io.konig.webapp;

import java.io.ByteArrayInputStream;

/*
 * #%L
 * konig-shacl
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.io.ContextReader;


public class JsonldContextServlet extends KonigServlet {
	private static final long serialVersionUID = 1L;
	private ContextReader contextReader = new ContextReader();
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_LENGTH = "Content-Length";

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		save(request, response, HttpServletResponse.SC_CREATED);
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		save(request, response, HttpServletResponse.SC_OK);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String iri = request.getRequestURL().toString();
		String canonicalIRI = config.getRewriteService().fromLocal(iri);
		
		Context context = config.getContextManager().getContextByURI(canonicalIRI);
		if (context == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not Found");
			return;
		}
		
		String text = context.toString();
		
		response.setHeader(CONTENT_TYPE, "application/ld+json");
		response.setHeader(CONTENT_LENGTH, Integer.toString(text.length()));
		
		response.getWriter().print(text);
		response.setStatus(HttpServletResponse.SC_OK);
	}

	protected void save(HttpServletRequest request, HttpServletResponse response, int statusCode) throws ServletException, IOException {

		InputStream input = request.getInputStream();
		
		Context context = contextReader.read(input);
		
		String iri = context.getContextIRI();
		if (iri == null) {
			response.sendError(
				HttpServletResponse.SC_BAD_REQUEST, 
				"@id property must be defined for the context");
			
			return;
		}
		
		ContextManager manager = config.getContextManager();
		try {
			manager.add(context);
		} catch (Throwable oops) {
			throw new ServletException(oops);
		}
		
		if (statusCode == HttpServletResponse.SC_CREATED) {
			String localIRI = config.getRewriteService().toLocal(iri);
			response.setHeader("Content-Location", localIRI);
		}
		response.setStatus(statusCode);
	}

}

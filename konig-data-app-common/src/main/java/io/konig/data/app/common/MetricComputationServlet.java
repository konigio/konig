package io.konig.data.app.common;

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


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

abstract public class MetricComputationServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException {
		String resource = (String) req.getParameter("resource");
		String query = readFile(resource);
		try {
			MarkedPath path = new MarkedPath(req.getPathInfo());
			String slug = path.currentElement();
			createDataComputationService(slug, query);
		} catch (Throwable e) {
			throw new ServletException(e);
		}		
	}
	
	
	abstract protected void createDataComputationService(String slug, String query) throws ServletException;
	
	private String readFile(String fileName) throws ServletException {
		try {
			return IOUtils.toString(getClass().getClassLoader()
					.getResourceAsStream("BigQueryMetricsExport/"+fileName));
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

}

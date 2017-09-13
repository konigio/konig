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


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.URI;

import io.konig.dao.core.ShapeReadService;
import io.konig.yaml.YamlReader;

abstract public class DataAppServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private DataApp dataApp;
	
	@Override
	public void init() throws ServletException {
		String configFile = getServletConfig().getInitParameter("configFile");
		
		if (configFile == null) {
			throw new ServletException("configFile init parameter is not defined");
		}
		try {
			try (
				InputStream input = getClass().getClassLoader().getResourceAsStream(configFile);
				Reader reader = new InputStreamReader(input);
				YamlReader yaml = new YamlReader(reader);
			) {
				yaml.addDeserializer(URI.class, new UriDeserializer());
				dataApp = yaml.readObject(DataApp.class);
				configure();
			}
		} catch (Throwable e) {
			throw new ServletException(e);
		}
		
	}

	private void configure() throws ServletException {
		ShapeReadService shapeReadService = createShapeReadService();
		for (ExtentContainer extent : dataApp.getContainers()) {
			extent.setShapeReadService(shapeReadService);
		}
	}

	public DataApp getDataApp() {
		return dataApp;
	}



	public void setDataApp(DataApp dataApp) {
		this.dataApp = dataApp;
	}

	
	abstract protected ShapeReadService createShapeReadService() throws ServletException;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException {
		resp.addHeader("Access-Control-Allow-Origin","*");
	    resp.addHeader("Access-Control-Allow-Methods","GET,POST");
	    resp.addHeader("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");
		JobRequest request = new JobRequest();
		request.setPath(req.getPathInfo());
		request.setQueryString(req.getQueryString());
		request.setWriter(resp.getWriter());
		
		try {
			GetJob job = dataApp.createGetJob(request);
			job.execute();
		} catch (DataAppException e) {
			resp.sendError(e.getStatusCode(), e.getMessage());
		}
	}

}

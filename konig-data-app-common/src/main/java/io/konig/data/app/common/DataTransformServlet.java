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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

abstract public class DataTransformServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, java.io.IOException {
		
		String resource = (String) req.getParameter("resource");
		String queryString = readFile(resource);
		HashMap<String, String> queryParams = new HashMap<String, String>();
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
		Calendar yesterday = Calendar.getInstance();
	    yesterday.add(Calendar.DATE, -1);
	    String date = dateformat.format(new Date(yesterday.getTimeInMillis()));	    		
		queryParams.put("eventCategory", (String) req.getParameter("eventCategory"));
		queryParams.put("fromDate", (String)req.getParameter("fromDate") == null? date :(String) req.getParameter("fromDate") );
		queryParams.put("toDate", (String)req.getParameter("toDate") == null? date :(String) req.getParameter("toDate"));
		createDataTransformService(queryString, queryParams);
	}

	abstract protected void createDataTransformService(String queryString, HashMap<String, String> queryParams)
			throws ServletException;

	private String readFile(String fileName) throws ServletException {
		try {
			return IOUtils.toString(getClass().getClassLoader()
					.getResourceAsStream("Fucntions/"+fileName));
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}
}

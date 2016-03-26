package io.konig.webapp;

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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.konig.services.KonigConfig;

public class KonigServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected KonigConfig config;

	
	public void init(ServletConfig servletConfig) throws ServletException {
		String className = servletConfig.getInitParameter("KonigConfig.class");
		try {
			Class<?> type = Class.forName(className);
			Method method = type.getMethod("getInstance");
			config = (KonigConfig) method.invoke(null);
			
		} catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | 
				SecurityException | IllegalArgumentException | InvocationTargetException e) {
			throw new ServletException(e);
		}
	}

}

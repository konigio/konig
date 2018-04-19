package io.konig.content.gae;

import java.io.BufferedReader;

/*
 * #%L
 * Konig Content System, Google App Engine implementation
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import io.konig.content.ContentAccessException;


public class GaeContentSystemServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String ZIP_CONTENT_TYPE = "application/zip";
	public static final String ALLOWED_DOMAINS = "allowed-domains";
	private static HashSet<String> domains = new HashSet<String>();
	private GaeCheckInBundleServlet checkInBundle = new GaeCheckInBundleServlet();
	private GaeAssetServlet asset = new GaeAssetServlet();
	private GaeZipBundleServlet zipBundle = new GaeZipBundleServlet();
	
	@Override
	public void init() throws ServletException {
		String configFile = getServletConfig().getInitParameter("configFile");
		
		if (configFile == null) {
			throw new ServletException("configFile init parameter is not defined");
		}
		try {
			InputStream input = getClass().getClassLoader().getResourceAsStream(configFile);
			Reader reader = new InputStreamReader(input);
			try (BufferedReader bufferreader = new BufferedReader(reader)) {
	            for (;;) {
	                String line = bufferreader.readLine();
	                if(line==null)
	                	break;
	                if (line.trim().length() > 0) {
	                	domains.add(line);
	                }
	            }
	        }
		} catch (Throwable e) {
			throw new ServletException(e);
		}
	}
	
	protected void doPost(HttpServletRequest req,  HttpServletResponse resp)
	throws ServletException, IOException {
		
		if (isBundleRequest(req)) {
			if (ZIP_CONTENT_TYPE.equals(req.getContentType())) {
				zipBundle.doPost(req, resp);
			} else {
				checkInBundle.doPost(req, resp);
			}
		} else {
			asset.doPost(req, resp);
		}
	}
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		UserService userService = UserServiceFactory.getUserService();
		String thisUrl = req.getRequestURI();
		User user = userService.getCurrentUser();
		if(user == null) {
			resp.sendRedirect(userService.createLoginURL(thisUrl));
		}
		
		if(!isValidDomain(user.getEmail())) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
						"<p>Invalid User Credential. Please <a href=\"" + userService.createLogoutURL(thisUrl) + "\">sign in</a> with valid domain</p>");
		}
		
		if (user != null && isValidDomain(user.getEmail())) {
			asset.doGet(req, resp);
		} 
		
	}
	

	private boolean isValidDomain(String email) throws ServletException {
		for(String validdomain : domains) {
			if(email.endsWith(validdomain)){
				return true;
			}
		}
		return false;
	}
	
	private boolean isBundleRequest(HttpServletRequest req) {
		String pathInfo = req.getPathInfo();
		int bundleNameEnd = pathInfo.indexOf('/', 1);
		int versionEnd = pathInfo.indexOf('/', bundleNameEnd+1);
		
		return versionEnd<0 || pathInfo.length()==versionEnd+1;
	}
}

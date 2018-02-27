package io.konig.data.app.common;

import java.io.IOException;

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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.openrdf.model.URI;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;

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
	
	private static String convertToSHA256String(String value) throws ServletException {
		try {			
			MessageDigest  md = MessageDigest.getInstance("SHA-256");
			byte[] bytes = md.digest(value.getBytes());
			StringBuffer stringBuffer = new StringBuffer();
	        for (int i = 0; i < bytes.length; i++) {
	            stringBuffer.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
	                    .substring(1));
	        }
	        return stringBuffer.toString();
		}catch(Throwable e) {
			throw new ServletException(e);
		}
	}
	
	private String getAuthenticationDetails(String username) throws ServletException {
																	  
		DatastoreOptions options = DatastoreOptions.newBuilder().build();
		Query<?> query = Query.newGqlQueryBuilder("Select * from UserAuthentication "
				+ "where username = @username")
				.setBinding("username", username)
				.build();
		Datastore datastore = options.getService();
		QueryResults<?> results = datastore.run(query);
		while (results.hasNext()) {
			Entity currentEntity = (Entity) results.next();
			return currentEntity.getString("passwordSha256");
		}
		return null;
	}
	
	private void configure() throws ServletException {
		ShapeReadService shapeReadService = createShapeReadService();
		for (ExtentContainer extent : dataApp.getContainer()) {
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
	
	private boolean validate(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String authHeader = req.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				if (basic.equalsIgnoreCase("Basic")) {
					try {
						String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
						int p = credentials.indexOf(":");
						if (p != -1) {
							String _username = credentials.substring(0, p).trim();
							String _password = convertToSHA256String(credentials.substring(p + 1).trim());
							String configPassword = getAuthenticationDetails(_username);
							if(configPassword == null) {
								unauthorized(resp, "Invalid authentication token");
							
							}
							if (!configPassword.equals(_password)) {
								unauthorized(resp, "Bad credentials");
							} else {
								return true;
							}
							
						} else {
							unauthorized(resp, "Invalid authentication token");
						}
					} catch (UnsupportedEncodingException e) {
						throw new Error("Couldn't retrieve authentication", e);
					}
				}
			}
		}
		return false;
	}

	private void unauthorized(HttpServletResponse response, String message) throws IOException {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { 
	    resp.setHeader("Access-Control-Allow-Origin", "*");
	    resp.addHeader("Access-Control-Allow-Methods","GET,POST");  
	    resp.addHeader("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept, authorization");
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, java.io.IOException {
		resp.addHeader("Access-Control-Allow-Origin","*");
	    resp.addHeader("Access-Control-Allow-Methods","GET,POST");
	    resp.addHeader("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept, authorization");
	    if(validate(req,resp)) {
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
	    }else {
			unauthorized(resp, "Invalid authentication token");
		}
	}

}

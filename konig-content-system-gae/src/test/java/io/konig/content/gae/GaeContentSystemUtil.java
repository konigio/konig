package io.konig.content.gae;

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


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GaeContentSystemUtil {
	
	static public String doGet(String path) throws Exception {
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		
		MockServletOutputStream buffer = new MockServletOutputStream();
		
		
		
		when(request.getPathInfo()).thenReturn(path);
		when(response.getOutputStream()).thenReturn(buffer);
		

		GaeAssetServlet servlet = new GaeAssetServlet();
		servlet.doGet(request, response);
		
		return buffer.toString();
	}
	

	static public void doPost(String path, String payload) throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		
		ByteArrayInputStream bytes = new ByteArrayInputStream(payload.getBytes());
		
		ServletInputStream input = new MockServletInputStream(bytes);
		
		StringWriter buffer = new StringWriter();
		when(request.getPathInfo()).thenReturn(path);
		when(request.getInputStream()).thenReturn(input);
		when(response.getWriter()).thenReturn(new PrintWriter(buffer));
		
		GaeAssetServlet servlet = new GaeAssetServlet();
		servlet.doPost(request, response);
		
	}
}

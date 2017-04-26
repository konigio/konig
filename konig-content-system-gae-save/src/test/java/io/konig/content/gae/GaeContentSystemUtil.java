package io.konig.content.gae;

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

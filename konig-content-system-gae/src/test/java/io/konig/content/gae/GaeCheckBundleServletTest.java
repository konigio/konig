package io.konig.content.gae;

import static io.konig.content.gae.GaeContentSystemUtil.doPost;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Ignore;
import org.junit.Test;

public class GaeCheckBundleServletTest extends DatastoreTest {
	
	@Ignore
	public void testCheckBundle() throws Exception {

		doPost("quotes/1.0/shakespeare/hamlet.txt", "To thine own self be true");
		doPost("quotes/1.0/shakespeare/macbeth.txt", "Something wicked this way comes");
		
		String text = "format=Bundle-1.0,name=shakespeare,version=1.1\n" +
				"quotes/1.1/shakespeare/hamlet.txt,5x-j97k6uaofuqlj3KlAcpN4NhQ\r\n" + 
				"quotes/1.1/shakespeare/macbeth.txt,foobar\r\n" + 
				"quotes/1.1/shakespeare/othello.txt,gobbledygook";
		
		
		StringWriter buffer = new StringWriter();

		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		
		when(request.getReader()).thenReturn(new BufferedReader(new StringReader(text)));
		when(response.getWriter()).thenReturn(new PrintWriter(buffer));
		
		GaeCheckInBundleServlet servlet = new GaeCheckInBundleServlet();
		servlet.doPost(request, response);
		
		String actual = buffer.toString().replace("\r", "");
		String expected =
			"quotes/1.1/shakespeare/macbeth.txt\n" + 
			"quotes/1.1/shakespeare/othello.txt\n";
		assertEquals(expected, actual);
		
		
	}
	

}

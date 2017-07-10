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

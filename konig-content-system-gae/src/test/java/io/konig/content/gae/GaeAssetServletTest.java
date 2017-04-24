package io.konig.content.gae;

import static io.konig.content.gae.GaeContentSystemUtil.doGet;
import static io.konig.content.gae.GaeContentSystemUtil.doPost;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GaeAssetServletTest extends DatastoreTest {
	


	@Test
	public void testPostGet() throws Exception {
		
		doPost("/quotes/1.0/shakespeare/hamlet.txt", "To thine own self be true");
		
		String actual = doGet("/quotes/1.0/shakespeare/hamlet.txt");

		assertEquals("To thine own self be true", actual);
	}


	

}

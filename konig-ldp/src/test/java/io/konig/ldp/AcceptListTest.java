package io.konig.ldp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AcceptListTest {

	@Test
	public void test() {
		String header = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
		float delta = 0.0001f;
		
		AcceptList list = new AcceptList();
		list.parse(header);
		list.sort();
		
		assertEquals(4, list.size());
		
		AcceptableMediaType m = list.get(0);
		assertEquals("application/xhtml+xml", m.getMediaType().getFullName());
		assertEquals(1, m.getQValue(), delta);
		
		m = list.get(1);
		assertEquals("text/html", m.getMediaType().getFullName());
		assertEquals(1, m.getQValue(), delta);
		
		m = list.get(2);
		assertEquals("application/xml", m.getMediaType().getFullName());
		assertEquals(0.9, m.getQValue(), delta);

		
		m = list.get(3);
		assertEquals("*/*", m.getMediaType().getFullName());
		assertEquals(0.8, m.getQValue(), delta);
		
		
	}
	
	

}

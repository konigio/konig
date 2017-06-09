package io.konig.content.client;

import static org.junit.Assert.*;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

public class LinkUtilTest {

	@Test
	public void test() {
		Header[] list = new Header[] {
			new BasicHeader("Link", "<gs://foo-bar>; rel=edit")
		};
		
		String actual = LinkUtil.getLink(list, "edit");
		
		assertEquals("gs://foo-bar", actual);
	}

}

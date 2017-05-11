package io.konig.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class PathUtilTest {

	@Test
	public void test() {
		String actual = PathUtil.toFilePath("http://example.com/foo");
		assertEquals("http/example.com/foo", actual);
	}

}

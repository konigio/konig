package io.konig.appengine;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import io.konig.core.io.ResourceFile;
import io.konig.core.io.impl.ResourceFileImpl;

public class GaeResourceManagerTest extends GaeTest {

	@Test
	public void test() throws Exception {
		
		GaeResourceManager manager = new GaeResourceManager();
		
		String location = "http://example.com/file";
		byte[] entityBody = "Hello World!".getBytes();
		ResourceFile file = new ResourceFileImpl(entityBody, new Properties());
		file.setProperty(ResourceFile.CONTENT_TYPE, "text/plain");
		file.setProperty(ResourceFile.CONTENT_LOCATION, location);
		
		manager.put(file);
		
		ResourceFile loaded = manager.get(location);
		
		String text = loaded.asText();
		
		assertEquals("Hello World!", text);
		assertEquals(location, loaded.getContentLocation());
		assertEquals("text/plain", loaded.getContentType());
		
	}

}

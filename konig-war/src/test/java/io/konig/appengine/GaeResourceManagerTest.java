package io.konig.appengine;

/*
 * #%L
 * konig-appengine
 * %%
 * Copyright (C) 2015 Gregory McFall
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

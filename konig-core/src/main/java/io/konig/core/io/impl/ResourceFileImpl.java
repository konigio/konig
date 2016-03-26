package io.konig.core.io.impl;

/*
 * #%L
 * konig-core
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


import java.util.Enumeration;
import java.util.Properties;

import io.konig.core.io.ResourceFile;
import static io.konig.core.io.HttpHeaders.*;

public class ResourceFileImpl implements ResourceFile {
	
	private byte[] entityBody;
	private Properties properties;
	
	public static ResourceFile create(String contentLocation, String contentType, String entityBody) {
		byte[] data = entityBody.getBytes();
		Properties p = new Properties();
		p.setProperty(CONTENT_LOCATION, contentLocation);
		p.setProperty(CONTENT_TYPE, contentType);
		return new ResourceFileImpl(data, p);
	}

	public ResourceFileImpl(byte[] entityBody, Properties properties) {
		this.entityBody = entityBody;
		this.properties = properties;
	}

	@Override
	public byte[] getEntityBody() {
		return entityBody;
	}

	@Override
	public String getContentLocation() {
		return properties.getProperty(CONTENT_LOCATION);
	}

	@Override
	public String getContentType() {
		return properties.getProperty(CONTENT_TYPE);
	}

	@Override
	public String asText() {
		return new String(entityBody);
	}

	@Override
	public String getProperty(String key) {
		
		return properties.getProperty(key);
	}

	@Override
	public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}
	
	public Properties getProperties() {
		return properties;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<String> propertyNames() {
		return (Enumeration<String>) properties.propertyNames();
	}

	@Override
	public ResourceFile replaceContent(byte[] entityBody) {
		Properties p = new Properties();
		Enumeration<String> sequence = propertyNames();
		while (sequence.hasMoreElements()) {
			String key = sequence.nextElement();
			p.setProperty(key, getProperty(key));
			
		}
		return new ResourceFileImpl(entityBody, p);
	}

	@Override
	public void setContentLocation(String contentLocation) {
		setProperty(CONTENT_LOCATION, contentLocation);
		
	}

	@Override
	public void setContentType(String contentType) {
		setProperty(CONTENT_TYPE, contentType);
	}

}

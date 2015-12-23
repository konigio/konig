package io.konig.core.io.impl;

import java.util.Enumeration;
import java.util.Properties;

import io.konig.core.io.ResourceFile;
import static io.konig.core.io.HttpHeaders.*;

public class ResourceFileImpl implements ResourceFile {
	
	private byte[] entityBody;
	private Properties properties;

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

}

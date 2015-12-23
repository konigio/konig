package io.konig.core.io;

import java.util.Enumeration;

public interface ResourceFile {
	public static final String CONTENT_LOCATION = "Content-Location";
	public static final String CONTENT_TYPE = "Content-Type";

	String asText();
	
	byte[] getEntityBody();
	String getContentLocation();
	String getContentType();

	Enumeration<String> propertyNames();
	String getProperty(String key);
	void setProperty(String key, String value);
	
	/**
	 * Create a new ResourceFile with the same properties as this one but a different entity body.
	 * @param entityBody The new entity body
	 * @return A new ResourceFile with the same properties as this one and the specified entity body.
	 */
	ResourceFile replaceContent(byte[] entityBody);
	

}

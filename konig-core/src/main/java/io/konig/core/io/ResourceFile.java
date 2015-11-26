package io.konig.core.io;

import java.util.Enumeration;

public interface ResourceFile {

	String asText();
	
	byte[] getEntityBody();
	String getContentLocation();
	String getContentType();

	Enumeration<String> propertyNames();
	String getProperty(String key);
	void setProperty(String key, String value);
	

}

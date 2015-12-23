package io.konig.appengine;

import java.util.Enumeration;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.Iterators;

import io.konig.core.io.ResourceFile;

public class GaeResourceFile implements ResourceFile {
	private static final String BODY = "body";
	
	private Entity entity;

	public GaeResourceFile(Entity entity) {
		this.entity = entity;
	}
	
	Entity getEntity() {
		return entity;
	}

	@Override
	public String asText() {
		Blob blob = (Blob) entity.getProperty(BODY);
		byte[] data = blob.getBytes();
		return new String(data);
	}

	@Override
	public byte[] getEntityBody() {
		Blob blob = (Blob) entity.getProperty(BODY);
		return blob.getBytes();
	}

	@Override
	public String getContentLocation() {
		
		return (String) entity.getProperty(CONTENT_LOCATION);
	}

	@Override
	public String getContentType() {
		return (String) entity.getProperty(CONTENT_TYPE);
	}

	@Override
	public Enumeration<String> propertyNames() {
		return Iterators.asEnumeration(entity.getProperties().keySet().iterator());
	}

	@Override
	public String getProperty(String key) {
		return (String) entity.getProperty(key);
	}

	@Override
	public void setProperty(String key, String value) {
		entity.setProperty(key, value);
	}

	@Override
	public ResourceFile replaceContent(byte[] entityBody) {
		Blob blob = new Blob(entityBody);
		entity.setProperty(BODY, blob);
		return this;
	}

}

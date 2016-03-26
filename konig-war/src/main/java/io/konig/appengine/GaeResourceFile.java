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


import java.util.Enumeration;

import static io.konig.appengine.GaeConstants.BODY;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.Iterators;

import io.konig.core.io.ResourceFile;

public class GaeResourceFile implements ResourceFile {
	
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

	@Override
	public void setContentLocation(String contentLocation) {
		setProperty(CONTENT_LOCATION, contentLocation);
		
	}

	@Override
	public void setContentType(String contentType) {
		setProperty(CONTENT_TYPE, contentType);
		
	}

}

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


import static io.konig.appengine.GaeConstants.Thing;

import java.io.IOException;
import java.util.Enumeration;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import io.konig.core.io.ResourceFile;
import io.konig.core.io.ResourceManager;

public class GaeResourceManager implements ResourceManager {
	
	private static Key createKey(String iri) {

		String hash = HashUtil.sha1Base64(iri);
		Key key = KeyFactory.createKey(Thing, hash);
		return key;
	}

	@Override
	public void delete(String contentLocation) throws IOException {
		
		Key key = createKey(contentLocation);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.delete(key);
	}

	@Override
	public ResourceFile get(String contentLocation) throws IOException {
		

		String hash = HashUtil.sha1Base64(contentLocation);
		Key key = KeyFactory.createKey(Thing, hash);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		try {
			Entity entity = datastore.get(key);
			return new GaeResourceFile(entity);
			
		} catch (EntityNotFoundException e) {
			
		}
		
		return null;
	}

	@Override
	public void put(ResourceFile file) throws IOException {
		Entity entity = null;
		
		if (file instanceof GaeResourceFile) {
			GaeResourceFile entityFile = (GaeResourceFile) file;
			entity = entityFile.getEntity();
		} else {
			Key key = createKey(file.getContentLocation());
			entity = new Entity(key);
			
			GaeResourceFile entityFile = new GaeResourceFile(entity);
			
			Enumeration<String> sequence = file.propertyNames();
			while (sequence.hasMoreElements()) {
				String name = sequence.nextElement();
				String value = file.getProperty(name);
				
				entityFile.setProperty(name, value);
			}
			entityFile.replaceContent(file.getEntityBody());
		}
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(entity);

	}

}

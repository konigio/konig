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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import io.konig.core.io.ResourceFile;
import io.konig.core.io.ResourceManager;
import io.konig.core.io.impl.ResourceFileImpl;

public class GaeResourceManager implements ResourceManager {
	private static final DatastoreService DS = DatastoreServiceFactory.getDatastoreService();
	
	public static Key createKey(String iri) {

		Key key = KeyFactory.createKey(Thing, iri);
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
		

		Key key = KeyFactory.createKey(Thing, contentLocation);
		
		try {
			Entity entity = DS.get(key);
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
		
		DS.put(entity);

	}

	@Override
	public ResourceFile createResource(String location, String type, String entityBody) {
		return ResourceFileImpl.create(location, type, entityBody);
	}

	@Override
	public Collection<ResourceFile> get(Iterable<String> resourceLocations) throws IOException {
		List<ResourceFile> result = new ArrayList<>();
		List<Key> keyList = toKeyList(resourceLocations);
		Map<Key,Entity> map = DS.get(keyList);
		for (Entity entity : map.values()) {
			GaeResourceFile file = new GaeResourceFile(entity);
			result.add(file);
		}
		
		return result;
	}

	private List<Key> toKeyList(Iterable<String> resourceLocations) {
		List<Key> list = new ArrayList<>();
		for (String location : resourceLocations) {
			list.add(createKey(location));
		}
		
		return list;
	}

}

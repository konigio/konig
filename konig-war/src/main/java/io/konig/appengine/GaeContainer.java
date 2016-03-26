package io.konig.appengine;

/*
 * #%L
 * konig-appengine
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import static io.konig.appengine.GaeConstants.Container;
import static io.konig.appengine.GaeConstants.Member;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import io.konig.core.Container;

public class GaeContainer implements Container {
	
	private URI containerId;
	private Key containerKey;
	
	private Map<String, URI> map = new HashMap<>();

	public GaeContainer(URI id) {
		containerId = id;
		containerKey = containerKey();

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		try {
			datastore.get(containerKey);
		} catch (EntityNotFoundException e) {
			Entity entity = new Entity(containerKey);
			datastore.put(entity);
		}
		
		reload();
	}

	
	synchronized public void reload() {
		Map<String,URI> newMap = new HashMap<>();

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(Member).setAncestor(containerKey);
		List<Entity> sequence = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
		for (Entity entity : sequence) {
			Key key = entity.getKey();
			String value = key.getName();
			URI member = new URIImpl(value);
			newMap.put(value, member);
		}
		
		map = newMap;
	}


	private Key containerKey() {
		Key key = KeyFactory.createKey(Container, getContainerId().stringValue());
		return key;
	}
	
	private Key memberKey(URI memberId) {
		return KeyFactory.createKey(containerKey, Member, memberId.stringValue());
	}


	@Override
	public void add(URI member) {
		
		String value = member.stringValue();
		map.put(value, member);
		Key memberKey = memberKey(member);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		try {
			datastore.get(memberKey);
		} catch (EntityNotFoundException e) {
			Entity entity = new Entity(memberKey);
			datastore.put(entity);
		}
		
	}

	@Override
	public void remove(URI member) {
		String value = member.stringValue();
		if (map.containsKey(value)) {
			map.remove(value);
		}
		Key memberKey = memberKey(member);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.delete(memberKey);
	}


	@Override
	public URI getContainerId() {
		return containerId;
	}


	@Override
	public Collection<URI> members() {
		return map.values();
	}

}

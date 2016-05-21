package io.konig.appengine.ldp;

/*
 * #%L
 * konig-war
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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import io.konig.ldp.BasicContainer;
import io.konig.ldp.Container;
import io.konig.ldp.LdpException;
import io.konig.ldp.ResourceFile;
import io.konig.ldp.ResourceType;
import io.konig.ldp.impl.RdfSourceImpl;

public class GaeBasicContainer extends RdfSourceImpl implements BasicContainer {
	
	private Key containerKey;
	
	public GaeBasicContainer(String contentLocation, String contentType, ResourceType type, byte[] body) {
		super(contentLocation, contentType, type, body);
		containerKey = GaeLDP.resourceKey(contentLocation);
	}

	@Override
	public Iterable<String> getMemberIds() {
		
		FilterPredicate filter = new FilterPredicate(GaeLDP.SUBJECT, FilterOperator.EQUAL, containerKey);
		Query q = new Query(GaeLDP.MEMBERSHIP).setFilter(filter);

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Iterable<Entity> results = ds.prepare(q).asIterable();
		List<String> list = new ArrayList<>();
		for (Entity e : results) {
			Key memberKey = (Key) e.getProperty(GaeLDP.OBJECT);
			String resourceId = GaeLDP.resourceId(memberKey);
			
			list.add(resourceId);
		}
		
		
		return list;
	}

	@Override
	public void remove(String resourceId) throws IOException, LdpException {
		
		Key resourceKey = GaeLDP.resourceKey(resourceId);
		
		FilterPredicate subjectFilter = new FilterPredicate(GaeLDP.SUBJECT, FilterOperator.EQUAL, containerKey);
		FilterPredicate objectFilter = new FilterPredicate(GaeLDP.OBJECT, FilterOperator.EQUAL, resourceKey);
		
		CompositeFilter filter = CompositeFilterOperator.and(subjectFilter, objectFilter);
		
		Query q = new Query(GaeLDP.MEMBERSHIP).setKeysOnly().setFilter(filter);
		
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Iterable<Entity> results = ds.prepare(q).asIterable();
		for (Entity e : results) {
			ds.delete(e.getKey());
		}

	}

	@Override
	public void add(ResourceFile member) throws IOException, LdpException {
		
		Key memberKey = GaeLDP.resourceKey(member.getContentLocation());
	
		Entity membership = new Entity(GaeLDP.MEMBERSHIP);
		membership.setProperty(GaeLDP.SUBJECT, containerKey);
		membership.setProperty(GaeLDP.OBJECT, memberKey);
		
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		ds.put(membership);
		
	}
	
	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public Container asContainer() {
		return (Container) this;
	}


	@Override
	public boolean isBasicContainer() {
		return true;
	}

	@Override
	public BasicContainer asBasicContainer() {
		return this;
	}
}

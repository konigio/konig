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
import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import io.konig.ldp.HttpStatusCode;
import io.konig.ldp.LdpException;
import io.konig.ldp.ResourceBuilder;
import io.konig.ldp.ResourceFile;
import io.konig.ldp.ResourceType;
import io.konig.ldp.impl.AbstractPlatform;

public class GaeLinkedDataPlatform extends AbstractPlatform {

	public GaeLinkedDataPlatform(String root) {
		super(root);
	}

	@Override
	public ResourceFile get(String resourceIRI) throws IOException, LdpException {
		
		Key resourceKey = GaeLDP.resourceKey(resourceIRI);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		try {
			Entity e = ds.get(resourceKey);
			return toResource(e);
		} catch (EntityNotFoundException e) {
		}
		
		return null;
	}

	@Override
	protected int save(ResourceFile resource) throws IOException {
		boolean exists = exists(resource.getContentLocation());
		
		Entity e = toEntity(resource);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		ds.put(e);
		
		return exists ? HttpStatusCode.OK : HttpStatusCode.CREATED;
	}

	private boolean exists(String resourceIRI) {

		Key resourceKey = GaeLDP.resourceKey(resourceIRI);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		FilterPredicate filter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, resourceKey);
		Query q = new Query(GaeLDP.RESOURCE).setFilter(filter).setKeysOnly();
		Iterator<Entity> sequence = ds.prepare(q).asIterable().iterator();
		
		
		return sequence.hasNext();
	}

	@Override
	protected void doDelete(String resourceIRI) throws IOException, LdpException {

		Key resourceKey = GaeLDP.resourceKey(resourceIRI);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		ResourceType type = getType(resourceKey);
		if (type != null) {

			switch (type) {
			case BasicContainer :
				if (!isEmpty(resourceKey)) {
					throw new LdpException("Cannot delete container because it is not empty", HttpStatusCode.BAD_REQUEST);
				}
				
			default:
				deleteMemberships(resourceKey);
				ds.delete(resourceKey);
				
			}
		}

	}

	/**
	 * Delete all membership records in which the specified resource is
	 * the object of the contains relationship.
	 */
	private void deleteMemberships(Key resourceKey) {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		FilterPredicate filter = new FilterPredicate(GaeLDP.OBJECT, FilterOperator.EQUAL, resourceKey);
		Query q = new Query(GaeLDP.MEMBERSHIP).setFilter(filter).setKeysOnly();
		
		Iterable<Entity> results = ds.prepare(q).asIterable();
		List<Key> list = new ArrayList<>();
		
		for (Entity e : results) {
			list.add(e.getKey());
		}
		
		if (!list.isEmpty()) {
			ds.delete(list);
		}
	}

	/**
	 * Return true if the specified container is empty and false otherwise
	 */
	private boolean isEmpty(Key resourceKey) {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		FilterPredicate filter = new FilterPredicate(GaeLDP.SUBJECT, FilterOperator.EQUAL, resourceKey);
		Query q = new Query(GaeLDP.MEMBERSHIP).setFilter(filter).setKeysOnly();
		
		Iterator<Entity> results = ds.prepare(q).asIterable(FetchOptions.Builder.withLimit(1)).iterator();
		
		return !results.hasNext();
	}

	private ResourceType getType(Key resourceKey) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		
		FilterPredicate filter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, resourceKey);
		
		Query q = new Query(GaeLDP.RESOURCE).setFilter(filter);
		q.addProjection(new PropertyProjection(GaeLDP.TYPE, Integer.class));
		
		Iterable<Entity> results = ds.prepare(q).asIterable(FetchOptions.Builder.withLimit(1));
		for (Entity e : results) {
			Long value = (Long) e.getProperty(GaeLDP.TYPE);
			return ResourceType.values()[value.intValue()];
		}
		
		return null;
	}

	private ResourceFile toResource(Entity e) {
		
		Key resourceKey = e.getKey();
		String resourceIRI = GaeLDP.resourceId(resourceKey);
		String contentType = (String) e.getProperty(GaeLDP.CONTENT_TYPE);
		Long ldpType = (Long) e.getProperty(GaeLDP.TYPE);
		Blob blob = (Blob) e.getProperty(GaeLDP.BODY);
		ResourceType type = ResourceType.values()[ldpType.intValue()];
		byte[] body = blob==null ? null : blob.getBytes();
		
		return getResourceBuilder()
			.contentLocation(resourceIRI)
			.contentType(contentType)
			.type(type)
			.entityBody(body)
			.resource();
	}
	
	
	private Entity toEntity(ResourceFile resource) {
		Key resourceKey = GaeLDP.resourceKey(resource.getContentLocation());
		Entity e = new Entity(resourceKey);
		Integer resourceType = resource.getType().ordinal();
		
		byte[] body = resource.getEntityBody();
		
		
		e.setProperty(GaeLDP.TYPE, resourceType);
		e.setProperty(GaeLDP.CONTENT_TYPE, resource.getContentType());
		
		if (body != null) {
			Blob blob =  new Blob(body);
			e.setProperty(GaeLDP.BODY, blob);
		}
		return e;
	}

	@Override
	public ResourceBuilder getResourceBuilder() {
		return new GaeResourceBuilder();
	}

}

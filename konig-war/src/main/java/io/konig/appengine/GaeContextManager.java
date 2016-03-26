package io.konig.appengine;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;

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


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.URI;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.impl.BasicContext;
import io.konig.core.impl.ContextTransformService;
import io.konig.core.io.ContextReader;

public class GaeContextManager implements ContextManager {


	private static final DatastoreService DS = DatastoreServiceFactory.getDatastoreService();
	

	private final MemcacheService mc = MemcacheServiceFactory.getMemcacheService();
	private ContextReader contextReader;
	private Context summary;
	private long lastVersion;
	private ContextTransformService contextTransformer = new ContextTransformService();
	
	private Map<Long, Context> byVersionNumber = new ConcurrentHashMap<>();
	private GaeSequence contextSequence;

	public GaeContextManager(ContextReader contextReader) {
		this(contextReader, "http://www.konig.io/context/universal");
	}
	public GaeContextManager(ContextReader contextReader, String summaryContextURI) {
		this.contextReader = contextReader;
		summary = getContextByURI(summaryContextURI);
		if (summary == null) {
			summary = new BasicContext(summaryContextURI);
			summary.setVersionNumber(GaeConstants.SUMMARY_CONTEXT_ID);
			lastVersion = GaeSequence.MIN_VALUE;
		}
		contextSequence = new GaeSequence(GaeConstants.LDContext);
	}
	
	
	private Key createKey(long id) {
		return KeyFactory.createKey(GaeConstants.LDContextVersion, id);
	}

	private Key createKey(String iri) {
		return KeyFactory.createKey(GaeConstants.LDContext, iri);
	}
	
	private Long getVersionNumberByIRI(Key contextKey) {
		String contextIRI = contextKey.getName();
		Long versionNumber = (Long) mc.get(contextIRI);
		
		if (versionNumber == null) {
			Query query = new Query(GaeConstants.LDContext, contextKey);
			query.addProjection(new PropertyProjection(GaeConstants.versionNumber, Long.class));
			
			Iterator<Entity> sequence = DS.prepare(query).asIterator(FetchOptions.Builder.withLimit(1));
			if (sequence.hasNext()) {
				Entity entity = sequence.next();
				versionNumber = (Long) entity.getProperty(GaeConstants.versionNumber);
			} 
			
		}
		
		return versionNumber;
	}
	

	private Long getVersionNumberByVendorType(String vendorType) {
		Long versionNumber = (Long) mc.get(vendorType);
		
		if (versionNumber == null) {
			Query query = new Query(GaeConstants.LDContext);
			query.setFilter(new FilterPredicate(GaeConstants.vendorType, FilterOperator.EQUAL, vendorType));
			query.addProjection(new PropertyProjection(GaeConstants.versionNumber, Long.class));
			
			Iterator<Entity> sequence = DS.prepare(query).asIterator(FetchOptions.Builder.withLimit(1));
			if (sequence.hasNext()) {
				Entity entity = sequence.next();
				versionNumber = (Long) entity.getProperty(GaeConstants.versionNumber);
			} else {
				versionNumber  = 0L;
			}
			
		}
		
		return versionNumber;
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listContexts() {
		
		List<String> list = null;
		try {
			list = (List<String>)mc.get(GaeConstants.LDContextList);
		} catch (Throwable ignore) {
		}
		
		if (list == null) {
			list = new ArrayList<>();
			Query query = new Query(GaeConstants.LDContext).setKeysOnly();
			Iterable<Entity> sequence = DS.prepare(query).asIterable();
			for (Entity entity : sequence) {
				String iri = entity.getKey().getName();
				list.add(iri);
			}
			try {
				mc.put(GaeConstants.LDContextList, list);
			} catch (Throwable ignore) {
			}
		}
		return list;
	}
	

	@Override
	synchronized public void add(Context context) {
		
		String iri = context.getContextIRI();
		if (iri == null) {
			throw new RuntimeException("Context IRI must be defined");
		}	
		long versionNumber = contextSequence.next();
		context.setVersionNumber(versionNumber);
		
		Entity version = toEntityVersion(context);
		Entity entity = toEntity(context);
		DS.put(entity);
		DS.put(version);
		byVersionNumber.put(versionNumber, context);
		encache(context);
		
		updateSummaryContext(context);
	}
	
	private void updateSummaryContext(Context context) {
		
		long contextVersion = context.getVersionNumber();
		long summaryVersion = lastVersion;
		
		Transaction txn = DS.beginTransaction();
		try {
			if (contextVersion != summaryVersion+1) {
				// Load all contexts after summaryVersion to catch up
				
				Query query = new Query(GaeConstants.LDContextVersion);
				query.setFilter(new FilterPredicate(GaeConstants.versionNumber, FilterOperator.GREATER_THAN, summaryVersion));
				
				Iterable<Entity> sequence = DS.prepare(query).asIterable();
			
				for (Entity entity : sequence) {
					Context c = toContextVersion(entity);
					contextTransformer.append(c, summary);
					summaryVersion = c.getVersionNumber();
				}
			} else {
				contextTransformer.append(context, summary);
				summaryVersion = contextVersion;
			}
			
			Entity summaryEntity = toEntityVersion(summary);
			summaryEntity.setProperty(GaeConstants.lastVersion, summaryVersion);
			
			DS.put(summaryEntity);
			txn.commit();
			
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
		
		
		
		
		
	}
	
	
	private void encache(Context context) {
		try {
			Long versionNumber = new Long(context.getVersionNumber());
			
			mc.put(context.getContextIRI(), versionNumber);
			if (context.getVendorType() != null) {
				mc.put(context.getVendorType(), versionNumber);
			}
			mc.delete(GaeConstants.LDContextList);
		} catch (Throwable ignore) {
			
		}
	}
	
	private Entity toEntityVersion(Context context) {
		long versionNumber = context.getVersionNumber();
	
		Key key = createKey(versionNumber);

		Text body = new Text(context.toString());
		
		String vendorType = context.getVendorType();
		
		Entity entity = new Entity(key);
		entity.setProperty(GaeConstants.IRI, context.getContextIRI());
		entity.setProperty(GaeConstants.BODY, body);
		if (vendorType != null) {
			entity.setProperty(GaeConstants.vendorType, vendorType);
		}
		
		return entity;
	}

	private Entity toEntity(Context context) {
		Key key = createKey(context.getContextIRI());
		
		String vendorType = context.getVendorType();
		
		Entity entity = new Entity(key);
		if (vendorType != null) {
			entity.setProperty(GaeConstants.vendorType, vendorType);
		}
		long version = context.getVersionNumber();
		if (version > 0) {
			entity.setProperty(GaeConstants.versionNumber, version);
		}
		
		return entity;
	}
	
	private Context toContextVersion(Entity entity) {

		long versionNumber = entity.getKey().getId();
		
		Text body = (Text) entity.getProperty(GaeConstants.BODY);
		
		ByteArrayInputStream stream = new ByteArrayInputStream(body.getValue().getBytes());
		Context context = contextReader.read(stream);
		context.setVersionNumber(versionNumber);
		context.setContextIRI((String)entity.getProperty(GaeConstants.IRI));
		return context;
	}

	@Override
	public Context getContextByURI(URI contextURI) {
		return getContextByURI(contextURI.stringValue());
	}

	@Override
	public Context getContextByURI(String contextURI) {

		Key key = createKey(contextURI);
		Long versionNumber = getVersionNumberByIRI(key);
		if (versionNumber == null) {
			return null;
		}
		
		Context context = byVersionNumber.get(versionNumber);
		if (context == null) {
			context = getContextByVersionNumber(versionNumber);
		}
		
		
		return context;
	}

	@Override
	public Context getContextByMediaType(String mediaType) {

		Long versionNumber = getVersionNumberByVendorType(mediaType);
		if (versionNumber == null) {
			return null;
		}
		
		Context context = byVersionNumber.get(versionNumber);
		if (context == null) {
			context = getContextByVersionNumber(versionNumber);
		}
		
		return context;
	}

	@Override
	public Context getContextByVersionNumber(long versionNumber) {
		Context context = byVersionNumber.get(versionNumber);
		if (context == null) {
			Key key = createKey(versionNumber);
			
			try {
				Entity entity = DS.get(key);
				context = toContextVersion(entity);
				byVersionNumber.put(versionNumber, context);
				
				if (versionNumber == GaeConstants.SUMMARY_CONTEXT_ID) {
					lastVersion = (Long) entity.getProperty(GaeConstants.lastVersion);
				}
				
			} catch (EntityNotFoundException ignore) {
			}
		}
		return context;
	}

	@Override
	public Context getUniversalContext() {
		
		long maxValue = contextSequence.lastValue();
		if (maxValue > lastVersion) {
			synchronized (this) {
				long summaryVersion = lastVersion;
				
				if (maxValue>lastVersion) {
					Query query = new Query(GaeConstants.LDContextVersion);
					query.setFilter(new FilterPredicate(GaeConstants.versionNumber, FilterOperator.GREATER_THAN, summaryVersion));
					
					Iterable<Entity> sequence = DS.prepare(query).asIterable();
				
					for (Entity entity : sequence) {
						Context c = toContextVersion(entity);
						contextTransformer.append(c, summary);
						summaryVersion = c.getVersionNumber();
					}
				}
				Entity updated = toEntityVersion(summary);
				updated.setProperty(GaeConstants.lastVersion, summaryVersion);
				DS.put(updated);
				lastVersion = summaryVersion;
			}
		}
		
		return summary;
	}
}

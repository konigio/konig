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


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class GaeSequence {

	public static final long MIN_VALUE = 100;
	private static final DatastoreService DS = DatastoreServiceFactory.getDatastoreService();
	
	public static long next(String sequenceName) {
		Key key = KeyFactory.createKey(GaeConstants.Sequence, sequenceName);
		return next(key);
		
	}
	
	private static long next(Key key) {
		long value = MIN_VALUE;
		Transaction txn = DS.beginTransaction();
		try {
			
			
			Entity entity = null;
			try {
				entity = DS.get(key);
				Long last = (Long) entity.getProperty(GaeConstants.last);
				value = last + 1;
				entity.setProperty(GaeConstants.last, value);
			} catch (EntityNotFoundException e) {
				entity = new Entity(key);
				entity.setProperty(GaeConstants.last, new Long(value));
			}
			DS.put(entity);
			txn.commit();
			
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
		
		return value;
	}
	
	private Key key;
	private String memcacheKey;
	private MemcacheService mc;
	
	public GaeSequence(String sequenceName) {
		key = KeyFactory.createKey(GaeConstants.Sequence, sequenceName);
		memcacheKey = sequenceName + "Sequence";
		mc = MemcacheServiceFactory.getMemcacheService();
	}
	
	public long next() {
		long result = next(key);
		Long value = mc.increment(memcacheKey, 1);
		if (value == null) {
			if (!mc.put(memcacheKey, new Long(result), null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT)) {
				// There was a race condition.  We cannot be sure if the value is accurate, so delete the cached value.
				mc.delete(memcacheKey);
			}
		}
		return result;
	}
	
	public long lastValue() {
		
		Long result = (Long) mc.get(memcacheKey);
		if (result == null) {
			try {
				Entity entity = DS.get(key);
				result = (Long) entity.getProperty(GaeConstants.last);
				if (!mc.put(memcacheKey, result, null, MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT)) {
					mc.delete(memcacheKey);
				}
			} catch (EntityNotFoundException e) {
				return 0;
			}
		}
		
		return result;
	}

}

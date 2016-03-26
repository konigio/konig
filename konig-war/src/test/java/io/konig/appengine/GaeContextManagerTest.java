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


import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import com.google.apphosting.api.ApiProxy;

import io.konig.core.Context;
import io.konig.core.io.ContextReader;

public class GaeContextManagerTest extends GaeTest  {
	
	

	@Test
	public void testGet() {
		
		ContextReader contextReader = new ContextReader();
		GaeContextManager manager = new GaeContextManager(contextReader);
		
		Context context = loadContext("context/person.jsonld");
		
		manager.add(context);
		
		Context actual = manager.getContextByURI(context.getContextIRI());
		String expected = context.toString();
		
		assertEquals(expected, actual.toString());
		
		actual = manager.getContextByMediaType(context.getVendorType());
		assertEquals(expected, actual.toString());
	}
	
	@Test
	public void testList() {

		ContextReader contextReader = new ContextReader();
		GaeContextManager manager = new GaeContextManager(contextReader);
		
		Context person = loadContext("context/person.jsonld");
		Context product = loadContext("context/product.jsonld");
		
		manager.add(person);
		manager.add(product);
		
		List<String> list = manager.listContexts();
		assertEquals(2, list.size());
		assertTrue(list.contains(person.getContextIRI()));
		assertTrue(list.contains(product.getContextIRI()));
	}
	
	private Context loadContext(String file) {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream(file);
		ContextReader reader = new ContextReader();
		
		return reader.read(input);
	}

}

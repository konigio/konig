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


import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class GaeContainerTest extends GaeTest {

	@Test
	public void test() {
		
		URI containerId = uri("http://example.com/container");
		URI one = uri("http://example.com/one");
		URI two = uri("http://example.com/two");
		URI three = uri("http://example.com/three");
		URI four = uri("http://example.com/four");
		
		GaeContainer container = new GaeContainer(containerId);
		container.add(one);
		container.add(two);
		container.add(three);
		
		Collection<URI> list = container.members();
		assertTrue(list.contains(one));
		assertTrue(list.contains(two));
		assertTrue(list.contains(three));
		
		container.remove(two);
		list = container.members();
		assertTrue(!list.contains(two));
		
		GaeContainer other = new GaeContainer(containerId);
		other.add(four);
		
		container.reload();
		list = container.members();
		assertTrue(list.contains(one));
		assertTrue(!list.contains(two));
		assertTrue(list.contains(three));
		assertTrue(list.contains(four));
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

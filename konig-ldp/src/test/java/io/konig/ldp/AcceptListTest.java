package io.konig.ldp;

/*
 * #%L
 * Konig Linked Data Platform
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


import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AcceptListTest {

	@Test
	public void test() {
		String header = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
		float delta = 0.0001f;
		
		AcceptList list = new AcceptList();
		list.parse(header);
		list.sort();
		
		assertEquals(4, list.size());
		
		AcceptableMediaType m = list.get(0);
		assertEquals("application/xhtml+xml", m.getMediaType().getFullName());
		assertEquals(1, m.getQValue(), delta);
		
		m = list.get(1);
		assertEquals("text/html", m.getMediaType().getFullName());
		assertEquals(1, m.getQValue(), delta);
		
		m = list.get(2);
		assertEquals("application/xml", m.getMediaType().getFullName());
		assertEquals(0.9, m.getQValue(), delta);

		
		m = list.get(3);
		assertEquals("*/*", m.getMediaType().getFullName());
		assertEquals(0.8, m.getQValue(), delta);
		
		
	}
	
	

}

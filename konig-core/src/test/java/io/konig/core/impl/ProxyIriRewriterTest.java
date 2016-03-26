package io.konig.core.impl;

/*
 * #%L
 * konig-core
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

import org.junit.Test;

public class ProxyIriRewriterTest {

	@Test
	public void test() {
		
		String canonicalBase = "http://www.konig.io/";
		String cacheBase = "http://localhost:8000/cache/";
		String localBase = "http://localhost:8000/";
		
		ProxyIriRewriteService service = new ProxyIriRewriteService(canonicalBase, cacheBase, localBase);
		
		String localAlice = service.toLocal("http://www.konig.io/resource/alice");
		
		assertEquals("http://localhost:8000/resource/alice", localAlice);

		String canonicalAlice = service.fromLocal(localAlice);
		assertEquals("http://www.konig.io/resource/alice", canonicalAlice);
		
		String schemaPerson = "http://schema.org/Person";
		
		String localPerson = service.toLocal(schemaPerson);
		assertEquals("http://localhost:8000/cache/http/schema.org/Person", localPerson);
		
		String canonicalPerson = service.fromLocal(localPerson);
		assertEquals(schemaPerson, canonicalPerson);
		
	}

}

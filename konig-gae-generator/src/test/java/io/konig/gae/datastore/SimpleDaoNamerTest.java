package io.konig.gae.datastore;

/*
 * #%L
 * Konig GAE Generator
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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
import static org.junit.Assert.fail;

import org.junit.Test;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.gae.datastore.SimpleDaoNamer;

public class SimpleDaoNamerTest {

	@Test
	public void test() {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		
		String basePackage = "com.example.gae.datastore";
		
		
		SimpleDaoNamer namer = new SimpleDaoNamer(basePackage, nsManager);
		
		String daoClass = namer.daoName(Schema.Person);
		assertEquals("com.example.gae.datastore.schema.PersonDao", daoClass);
		
	}
}

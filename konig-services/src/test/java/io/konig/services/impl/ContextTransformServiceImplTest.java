package io.konig.services.impl;

/*
 * #%L
 * Konig Services
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

import org.junit.Test;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.impl.ContextTransformService;
import io.konig.core.io.ContextReader;

public class ContextTransformServiceImplTest {
	
	private ContextReader contextReader = new ContextReader();

	@Test
	public void test() throws Exception {
		
		Context personContext = loadContext("person-context.jsonld");
		Context orgContext = loadContext("w3c-organization-context.jsonld");
		
		ContextTransformService service = new ContextTransformService();
		
		service.append(orgContext, personContext);
		
		assertEquals(6, personContext.asList().size());
		
		Term orgTerm = personContext.getTerm("org");
		assertTrue(orgTerm != null);
		assertEquals("http://www.w3.org/ns/org#", orgTerm.getId());
		
		Term organizationTerm = personContext.getTerm("org_Organization");
		assertTrue(orgTerm != null);
		assertEquals("http://www.w3.org/ns/org#Organization", organizationTerm.getId());
		
	}
	
	private Context loadContext(String path) throws Exception {
	
		InputStream input = getClass().getClassLoader().getResourceAsStream(path);
		
		return contextReader.read(input);
	}

}

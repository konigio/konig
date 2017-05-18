package io.konig.core.path;

/*
 * #%L
 * Konig Core
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


import static org.junit.Assert.*;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.NameMap;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;

public class PathContextBuilderTest {

	@Test
	public void test() {
		
		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		nsManager.add("category", "http://example.com/category/");
		
		URI electronics = uri("http://example.com/category/electronics");
		
		NameMap nameMap = new NameMap();
		nameMap.put("electronics", electronics);
		
		PathImpl path = new PathImpl();
		path.add(new OutStep(Schema.alumniOf));
		path.add(new InStep(Schema.brand));
		
		HasStep hasStep = new HasStep();
		hasStep.add(Schema.category, electronics);
		
		path.add(hasStep);
		
		PathContextBuilder.buildContext(path, nsManager);
		
		String actual = path.toString();
		
		String expected = 
			"@context {\n" + 
			"  \"schema\" : \"http://schema.org/\",\n" + 
			"  \"alumniOf\" : \"schema:alumniOf\",\n" + 
			"  \"brand\" : \"schema:brand\",\n" + 
			"  \"category\" : \"schema:category\",\n" + 
			"  \"electronics\" : \"http://example.com/category/electronics\"\n" + 
			"}\n" + 
			"/alumniOf^brand[category electronics]";
		
		assertEquals(expected, actual);
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

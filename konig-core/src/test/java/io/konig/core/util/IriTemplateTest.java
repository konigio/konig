package io.konig.core.util;

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

import io.konig.core.impl.BasicContext;

public class IriTemplateTest {
	
	@Test
	public void testToString() {
		
		BasicContext context = new BasicContext(null);
		context.addTerm("schema", "http://schema.org/");
		context.addTerm("name",	"schema:name");
		String text = "http://example.com/product/{name}";
		
		IriTemplate template = new IriTemplate(context, text);
		
		String actual = template.toString();
		
		String expected =
			"@context {\n" + 
			"   \"schema\" : \"http://schema.org/\",\n" + 
			"   \"name\" : \"schema:name\"\n" + 
			"}\n" + 
			"\n" + 
			"<http://example.com/product/{name}>";
		assertEquals(expected, actual);
		
	}

	@Test
	public void test() {
		
		SimpleValueMap map = new SimpleValueMap();
		map.put("foo", "alpha");
		map.put("bar", "beta");
		map.put("baseURL", "http://acme.com/");
		
		IriTemplate template = new IriTemplate("http://example.com/{foo}/{bar}");
		
		assertEquals("http://example.com/alpha/beta", template.expand(map).stringValue());
		
		template = new IriTemplate("{baseURL}{bar}/{foo}");
		assertEquals("http://acme.com/beta/alpha", template.expand(map).stringValue());
		
		template = new IriTemplate("{baseURL}{foo}/bar");
		assertEquals("http://acme.com/alpha/bar", template.expand(map).stringValue());
		
		
	}

}

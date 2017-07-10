package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO SQL Runtime
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

import java.io.StringReader;
import java.util.Map;

import org.junit.Test;

public class NamespaceReaderTest {

	@Test
	public void test() throws Exception {
		String text =
			"@prefix schema: <http://schema.org/> .\n" + 
			"@prefix org : <http://www.w3.org/ns/org#> .";
		
		StringReader reader = new StringReader(text);
		NamespaceReader nsReader = new NamespaceReader(reader);
		Map<String,String> map = nsReader.readNamespaces();
		
		String schemaPrefix = map.get("http://schema.org/");
		String orgPrefix = map.get("http://www.w3.org/ns/org#");
		
		assertEquals("schema", schemaPrefix);
		assertEquals("org", orgPrefix);
	}

}
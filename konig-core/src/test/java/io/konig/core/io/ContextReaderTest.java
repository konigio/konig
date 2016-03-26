package io.konig.core.io;

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


import java.io.InputStream;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Context;
import io.konig.core.Term;
import io.konig.core.impl.MemoryContextManager;

public class ContextReaderTest {

	@Test
	public void test() throws Exception {
		
		MemoryContextManager contextManager = new MemoryContextManager();
		contextManager.loadFromClasspath();
		
		InputStream input = getClass().getClassLoader().getResourceAsStream("samples/schemaShapes.jsonld");
		
		ContextReader contextReader = new ContextReader(contextManager);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(input);

		Context context = contextReader.parse((ObjectNode)node);
		
		Term term = context.getTerm("scopeClass");
		assertTrue(term != null);
		
		term = context.getTerm("schema");
		assertTrue(term != null);
		
		
		
	}

}

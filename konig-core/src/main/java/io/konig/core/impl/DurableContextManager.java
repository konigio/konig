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


import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import io.konig.core.Context;
import io.konig.core.io.ContextWriter;
import io.konig.core.io.ResourceFile;
import io.konig.core.io.ResourceManager;

public class DurableContextManager extends MemoryContextManager {

	private ResourceManager resourceManager;
	
	public DurableContextManager(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}
	

	public void add(Context context) {
		super.add(context);
		
		try {
			StringWriter out = new StringWriter();
			ContextWriter writer = new ContextWriter();
			JsonFactory factory = new JsonFactory();
			JsonGenerator generator = factory.createGenerator(out);
			generator.setPrettyPrinter(new DefaultPrettyPrinter());
			writer.write(context, generator);
			generator.flush();
			
			String text = out.toString();
			
			ResourceFile resource = resourceManager.createResource(context.getContextIRI(), "application/ld+json", text);
			resourceManager.put(resource);
			
		} catch (IOException e) {
			throw new RuntimeException("Failed to store context: " + context.getContextIRI());
		}
		
	}
	

}

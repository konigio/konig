package io.konig.services.impl;

/*
 * #%L
 * Konig Services
 * %%
 * Copyright (C) 2015 Gregory McFall
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
import java.io.InputStream;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.KonigException;
import io.konig.core.impl.ContextManagerImpl;
import io.konig.core.io.ContextReader;
import io.konig.core.io.ResourceManager;
import io.konig.core.io.impl.MemoryResourceManager;
import io.konig.services.GraphService;
import io.konig.services.KonigConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class BaseKonigConfig implements KonigConfig {
	
	private static final String defaultContextPath = "konig/context.jsonld";
	
	private Context defaultContext;
	private ShapeManager shapeManager;
	protected ResourceManager resourceManager;
	private GraphService graphService;
	private ContextManager contextManager;

	
	public Context getDefaultContext() {
		if (defaultContext == null) {
			InputStream input = getClass().getClassLoader().getResourceAsStream(defaultContextPath);
			if (input == null) {
				throw new KonigException("Context not found in classpath: " + defaultContextPath);
			}
			ContextReader reader = new ContextReader();
			defaultContext = reader.read(input);
			try {
				input.close();
			} catch (IOException e) {
			}
		}
		return defaultContext;
	}

	public ShapeManager getShapeManager() {
		if (shapeManager == null) {
			shapeManager = new MemoryShapeManager();
		}
		return shapeManager;
	}

	public ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new MemoryResourceManager();
		}
		return resourceManager;
	}

	public GraphService getGraphService() {
		if (graphService == null) {
			graphService = new GraphServiceImpl(getDefaultContext(), getContextManager(), getResourceManager(), getShapeManager());
		}
		return graphService;
	}

	public ContextManager getContextManager() {
		if (contextManager == null) {
			contextManager = new ContextManagerImpl();
			contextManager.add(getDefaultContext());
		}
		return contextManager;
	}

}

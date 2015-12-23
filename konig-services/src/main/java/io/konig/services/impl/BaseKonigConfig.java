package io.konig.services.impl;

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

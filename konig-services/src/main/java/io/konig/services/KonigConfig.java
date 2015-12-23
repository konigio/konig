package io.konig.services;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.io.ResourceManager;
import io.konig.shacl.ShapeManager;

public interface KonigConfig {
	
	Context getDefaultContext();
	ContextManager getContextManager();
	ShapeManager getShapeManager();
	ResourceManager getResourceManager();
	GraphService getGraphService();

}

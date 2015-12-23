package io.konig.appengine;

import io.konig.core.io.ResourceManager;
import io.konig.services.impl.BaseKonigConfig;

public class GaeKonigConfig extends BaseKonigConfig {
	
	private static GaeKonigConfig INSTANCE = new GaeKonigConfig();
	
	public GaeKonigConfig getInstance() {
		return INSTANCE;
	}

	public ResourceManager getResourceManager() {
		if (resourceManager == null) {
			resourceManager = new GaeResourceManager();
		}
		return resourceManager;
	}
}

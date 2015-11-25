package io.konig.core;

import org.openrdf.model.URI;

public interface ContextManager {
	
	void add(Context context);
	Context getContextByURI(URI contextURI);
	Context getContextByURI(String contextURI);

}

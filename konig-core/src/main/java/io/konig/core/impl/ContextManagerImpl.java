package io.konig.core.impl;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.core.Context;
import io.konig.core.ContextManager;

public class ContextManagerImpl implements ContextManager {
	
	private Map<String,Context> map = new HashMap<String, Context>();

	public Context getContextByURI(URI contextURI) {
		
		return map.get(contextURI.stringValue());
	}

	public Context getContextByURI(String contextURI) {
		return map.get(contextURI);
	}

	public void add(Context context) {
		map.put(context.getContextIRI(), context);
		
	}

}

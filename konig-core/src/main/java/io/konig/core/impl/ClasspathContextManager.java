package io.konig.core.impl;

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


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.io.ContextReader;
import io.konig.core.util.IOUtil;
import io.konig.core.util.PathUtil;

/**
 * A ContextManager that loads JSON-LD context resources from the classpath.
 * The JSON-LD context must be found in the classpath under the folder at "jsonld/context".
 * The path relative to this folder is derived from the JSON-LD context URL by replacing
 * ':' characters with '/' and collapsing multiple slashes into a single slash.
 * <p>
 * For example, the JSON-LD context whose URL is "http://example.com/ctx/somecontext"
 * would be accessed from the classpath at:
 * <pre>
 *   jsonld/context/http/example.com/ctx/somecontext
 * </pre>
 * @author Greg McFall
 *
 */
public class ClasspathContextManager implements ContextManager {
	
	private static final String BASE = "jsonld/context/";

	private Map<String,Context> contextMap = new HashMap<>();
	
	@Override
	public List<String> listContexts() {
		return new ArrayList<>(contextMap.keySet());
	}

	@Override
	public void add(Context context) {
		String id = context.getContextIRI();
		contextMap.put(id, context);
	}

	@Override
	public Context getContextByURI(URI contextURI) {
		return getContextByURI(contextURI.stringValue());
	}

	@Override
	public Context getContextByURI(String contextURI) {
		
		Context result = contextMap.get(contextURI);
		if (result == null) {

			String filePath = BASE + PathUtil.toFilePath(contextURI);
			InputStream input = getClass().getClassLoader().getResourceAsStream(filePath);
			try {
				ContextReader contextParser = new ContextReader(new ContextGetter());
				result = contextParser.read(input);
				result.compile();
				result.setContextIRI(contextURI);
				add(result);
				
			} finally {
				try {
					input.close();
				} catch (IOException e) {
					
				}
			}
			
		}
		
		return result;
	}

	@Override
	public Context getContextByMediaType(String mediaType) {
		return null;
	}

	@Override
	public Context getContextByVersionNumber(long versionNumber) {
		return null;
	}

	@Override
	public Context getUniversalContext() {
		return null;
	}
	
	class ContextGetter implements ContextManager {

		@Override
		public List<String> listContexts() {
			return ClasspathContextManager.this.listContexts();
		}

		@Override
		public void add(Context context) {
			ClasspathContextManager.this.add(context);
		}

		@Override
		public Context getContextByURI(URI contextURI) {
			return getContextByURI(contextURI.stringValue());
		}

		@Override
		public Context getContextByURI(String contextURI) {
			return contextMap.get(contextURI);
		}

		@Override
		public Context getContextByMediaType(String mediaType) {
			return null;
		}

		@Override
		public Context getContextByVersionNumber(long versionNumber) {
			return null;
		}

		@Override
		public Context getUniversalContext() {
			return null;
		}
		
	}

}

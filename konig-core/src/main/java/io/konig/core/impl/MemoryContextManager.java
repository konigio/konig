package io.konig.core.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/*
 * #%L
 * konig-core
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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Context;
import io.konig.core.ContextManager;
import io.konig.core.io.ContextReader;

public class MemoryContextManager implements ContextManager {
	private static final Logger logger = LoggerFactory.getLogger(MemoryContextManager.class);
	private static final String CLASSPATH_MANIFEST = "jsonld/context.properties";
	private static Long versionSequence=1L;
	
	private Map<String,Context> map = new HashMap<String, Context>();
	private Map<String,Context> byMediaType = new HashMap<>();
	private Map<Long, Context> byVersionNumber = new HashMap<>();
	private Context summary;
	private ContextTransformService transformer;
	
	public MemoryContextManager(Context summary) {
		this.summary = summary;
	}
	
	public MemoryContextManager() {
	}

	public Context getContextByURI(URI contextURI) {
		
		return map.get(contextURI.stringValue());
	}

	public Context getContextByURI(String contextURI) {
		return map.get(contextURI);
	}

	public void add(Context context) {
		map.put(context.getContextIRI(), context);
		
		if (context.getVersionNumber()<1) {
			context.setVersionNumber(versionSequence++);
		}
		
		String vendorType = context.getVendorType();
		if (vendorType != null) {
			byMediaType.put(vendorType, context);
		}
		
		byVersionNumber.put(context.getVersionNumber(), context);
		
		if (summary != null) {
			if (transformer == null) {
				transformer = new ContextTransformService();
			}
			transformer.append(context, summary);
		}
		
	}
	
	public void loadFromClasspath() throws IOException {
		
		InputStream input = getClass().getClassLoader().getResourceAsStream(CLASSPATH_MANIFEST);
		try {
			
		if (input != null) {
			Properties properties = new Properties();
			properties.load(input);
			ContextReader contextReader = new ContextReader();
			for (Object key : properties.keySet()) {
				String path = "jsonld/" + (String) key;
				String contextId = properties.getProperty(key.toString());
				InputStream contextInput = getClass().getClassLoader().getResourceAsStream(path);
				try {
					Context context = contextReader.read(contextInput);
					logger.debug("Loaded context {}", contextId);
					if (context.getContextIRI()==null) {
						context.setContextIRI(contextId);
					}
					add(context);
				} catch (Throwable ignore) {
					logger.debug("Failed to load context " + contextId, ignore);
				} finally {
					close(contextInput);
				}
			}
			
		}
		} finally {
			close(input);
		}
				
	}
	
	public void loadFromFile(File source) throws IOException {
		if (source.isDirectory()) {
			File[] kids = source.listFiles();
			for (File file : kids) {
				loadFromFile(file);
			}
		} else {
			String name = source.getName();
			if (name.endsWith(".json") || name.endsWith(".jsonld")) {
				ContextReader reader = new ContextReader();
				FileInputStream input = new FileInputStream(source);
				try {
					Context context = reader.read(input);
					if (context.getContextIRI() != null) {
						add(context);
					} else {
						logger.warn("Ignoring JSON-LD context because it does not have an @id value: "  + source.getAbsolutePath());
					}
				} finally {
					close(input);
				}
			}
				
		}
	}

	private void close(InputStream input) {
		if (input != null) {
			try {
				input.close();
			} catch (Throwable ignore) {
				
			}
		}
		
	}

	@Override
	public List<String> listContexts() {
		return new ArrayList<>(map.keySet());
	}

	@Override
	public Context getContextByMediaType(String mediaType) {
		return byMediaType.get(mediaType);
	}

	@Override
	public Context getContextByVersionNumber(long versionNumber) {
		return byVersionNumber.get(versionNumber);
	}

	@Override
	public Context getUniversalContext() {
		return summary;
	}


}

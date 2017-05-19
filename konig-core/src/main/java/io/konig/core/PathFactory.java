package io.konig.core;

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


import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.path.PathParseException;	

public class PathFactory {
	private static final Logger logger = LoggerFactory.getLogger(PathFactory.class);
	public static boolean RETURN_NULL_ON_FAILURE = false;
	
	private NamespaceManager nsManager;
	private NameMap nameMap;
	
	public PathFactory() {
		
	}
	
	public PathFactory(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}
	
	
	public PathFactory(NamespaceManager nsManager, NameMap nameMap) {
		this.nsManager = nsManager;
		this.nameMap = nameMap;
	}
	
	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}




	public Path createPath(String text) throws PathParseException {
		try {
			StringReader reader = new StringReader(text);
			io.konig.core.path.PathParser parser = new io.konig.core.path.PathParser(nsManager);
			parser.setNameMap(nameMap);
			
			return parser.path(reader);
		} catch (PathParseException e) {
			if (RETURN_NULL_ON_FAILURE) {
				logger.warn("Failed to parse path: {}", text);
				return null;
			} 
			throw e;
		}
	}
	
	
	

}
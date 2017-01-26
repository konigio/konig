package io.konig.core.path;

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

import io.konig.core.LocalNameService;
import io.konig.core.NamespaceManager;
import io.konig.core.Path;	

public class PathFactory {
	
	private NamespaceManager nsManager;
	private LocalNameService localNameService;
	
	public PathFactory(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}
	
	
	public PathFactory(NamespaceManager nsManager, LocalNameService localNameService) {
		this.nsManager = nsManager;
		this.localNameService = localNameService;
	}
	
	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}




	public Path createPath(String text) throws PathParseException {
		
		StringReader reader = new StringReader(text);
		io.konig.core.path.PathParser parser = new io.konig.core.path.PathParser(nsManager);
		parser.setLocalNameService(localNameService);
		
		return parser.path(reader);
	}
	
	
	

}
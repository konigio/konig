package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.io.File;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.KonigException;

public class FileUriFactory {
	
	private int basePathEnd;
	private String basePath;
	private String baseURI;
	
	public FileUriFactory(File baseDir, String baseURI) {
		basePath = baseDir.getAbsolutePath().replace('\\', '/');
		basePathEnd = basePath.lastIndexOf('/') + 1;
		this.baseURI = baseURI;
	}

	public URI toUri(File file) {
		String path = file.getAbsolutePath().replace('\\', '/');
		if (!path.startsWith(basePath)) {
			throw new KonigException("File is not contained within the baseDir: "  + path);
		}
		String tail = path.substring(basePathEnd);
		
		
		return new URIImpl(baseURI + tail);
		
	}
	
	
}

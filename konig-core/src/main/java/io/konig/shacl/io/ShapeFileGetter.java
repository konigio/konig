package io.konig.shacl.io;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.io.FileGetter;

public class ShapeFileGetter implements FileGetter {
	private File baseDir;
	private NamespaceManager nsManager;
	
	

	public ShapeFileGetter(File baseDir, NamespaceManager nsManager) {
		this.baseDir = baseDir;
		this.nsManager = nsManager;
	}


	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}

	@Override
	public File getFile(URI shapeId) {
		
		
		Namespace n = nsManager.findByName(shapeId.getNamespace());
		if (n == null) {
			return nestedShapeFile(shapeId);
		}
		
		
		
		StringBuilder builder = new StringBuilder();
		builder.append(n.getPrefix());
		builder.append('_');
		builder.append(shapeId.getLocalName());
		builder.append(".ttl");
		
		
		return new File(baseDir, builder.toString());
	}


	private File nestedShapeFile(URI shapeId) {
		
		String iriValue = shapeId.stringValue();
		for(int end = iriValue.lastIndexOf('/'); end>0; end=iriValue.lastIndexOf('/', end-1)) {
			
			int mark = end+1;
			String namespaceName = iriValue.substring(0, mark);
			Namespace ns = nsManager.findByName(namespaceName);
			if (ns != null) {
			
				String fileName = iriValue.substring(mark).replace('/', '.');
				StringBuilder builder = new StringBuilder();
				builder.append(ns.getPrefix());
				builder.append('_');
				builder.append(fileName);
				builder.append(".ttl");

				return new File(baseDir, builder.toString());
			}
		}

		throw new KonigException("Prefix for namespace not found: " + shapeId.getNamespace());
	}

}

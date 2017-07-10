package io.konig.showl;

/*
 * #%L
 * Konig Schema Generator
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


import java.io.File;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.io.FileGetter;

/**
 * A utility that gets the File that holds the description of a given ontology.
 * @author Greg McFall
 *
 */
public class OntologyFileGetter implements FileGetter {
	private File baseDir;
	private NamespaceManager nsManager;
	
	public OntologyFileGetter(File baseDir, NamespaceManager nsManager) {
		this.baseDir = baseDir;
		this.nsManager = nsManager;
	}

	@Override
	public File getFile(URI ontologyId) {
		
		Namespace namespace = nsManager.findByName(ontologyId.stringValue());
		if (namespace == null) {
			throw new KonigException("Prefix not found for namespace: " + ontologyId.stringValue());
		}
		String prefix = namespace.getPrefix();
		StringBuilder builder = new StringBuilder();
		builder.append(prefix);
		builder.append(".ttl");
		return new File(baseDir, builder.toString());
	}
	

}

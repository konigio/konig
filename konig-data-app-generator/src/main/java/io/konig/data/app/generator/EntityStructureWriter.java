package io.konig.data.app.generator;

/*
 * #%L
 * Konig Data App Generator
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
import java.io.FileWriter;
import java.io.IOException;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.sql.runtime.EntityStructure;
import io.konig.yaml.Yaml;

public class EntityStructureWriter {
	
	private NamespaceManager nsManager;
	private File baseDir;	
	
	public EntityStructureWriter(NamespaceManager nsManager, File baseDir) {
		this.nsManager = nsManager;
		this.baseDir = baseDir;
	}

	public void write(EntityStructure e, URI shapeId) throws KonigException, IOException {
		Namespace ns = nsManager.findByName(shapeId.getNamespace());
		if (ns == null) {
			throw new KonigException("Prefix not found for namespace: " + shapeId.getNamespace());
		}
		String prefix = ns.getPrefix();
		String localName = shapeId.getLocalName();
		File nsDir = new File(baseDir, prefix);
		nsDir.mkdirs();
		File outFile = new File(nsDir, localName);
		Yaml.write(outFile, e);
	}

}

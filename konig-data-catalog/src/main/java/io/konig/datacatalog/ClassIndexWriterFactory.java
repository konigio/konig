package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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
import java.io.PrintWriter;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.NamespaceManager;

public class ClassIndexWriterFactory implements WriterFactory {

	private File baseDir;
	
	public ClassIndexWriterFactory(File baseDir) {

		this.baseDir = baseDir;
		baseDir.mkdirs();
	}

	@Override
	public PrintWriter createWriter(PageRequest request, URI resourceId) throws IOException, DataCatalogException {
		File file = null;
		if (resourceId == null) {
			file = new File(baseDir, "allclasses-index.html");
		} else {
			NamespaceManager nsManager = request.getGraph().getNamespaceManager();
			if (nsManager == null) {
				throw new DataCatalogException("NamespaceManager is not defined");
			}
			Namespace ns = request.findNamespaceByName(resourceId.stringValue());
			file = new File(baseDir, DataCatalogUtil.classIndexFileName(ns));
		}
		return new PrintWriter(new FileWriter(file));
	}

}

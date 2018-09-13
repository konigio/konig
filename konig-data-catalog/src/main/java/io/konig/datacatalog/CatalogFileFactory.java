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

import io.konig.datasource.TableDataSource;

public class CatalogFileFactory {
	private static final String ARTIFACTS_FOLDER = "artifacts/";
	
	private File baseDir;
	

	public CatalogFileFactory(File baseDir) {
		this.baseDir = baseDir;
	}
	
	public URI catalogDdlFileIri(TableDataSource ds) {
		
		String path = catalogDdlPath(ds);
		return new URIImpl(DataCatalogBuilder.CATALOG_BASE_URI + path);
	}
	
	


	private String catalogDdlPath(TableDataSource ds) {
		StringBuilder builder = new StringBuilder();
		builder.append(ARTIFACTS_FOLDER);
		String dot = "";
		String identifier = ds.getUniqueIdentifier();
		String[] parts = identifier.split(":");
		for (String part : parts) {
			if (part.startsWith("$")) {
				continue;
			}
			builder.append(dot);
			dot = ".";
			builder.append(part);
		}
		builder.append(dot);
		builder.append("ddl");

		String ddlFileName = ds.getDdlFileName();
		int mark = ddlFileName.lastIndexOf('.');
		String suffix = ddlFileName.substring(mark);
		builder.append(suffix);
		return builder.toString();
	}

	public File catalogDdlFile(TableDataSource tableDatasource) {
		
		String path = catalogDdlPath(tableDatasource);
		return new File(baseDir, path);
	}

}

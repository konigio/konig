package io.konig.datasource;

/*
 * #%L
 * Konig Core
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

public class DdlFileLocator implements DatasourceFileLocator {

	private File baseDir;
	
	public DdlFileLocator(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public File locateFile(DataSource ds) {
		if (ds instanceof TableDataSource) {
			TableDataSource table = (TableDataSource) ds;
			String fileName = table.getUniqueIdentifier().replace(':', '_') + ".sql";
			return new File(baseDir, fileName);
		}
		return null;
	}

}

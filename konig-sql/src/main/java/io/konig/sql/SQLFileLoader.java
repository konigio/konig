package io.konig.sql;

/*
 * #%L
 * Konig SQL
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
import java.io.FileReader;
import java.io.IOException;

public class SQLFileLoader {

	private SQLParser parser;
	
	public SQLFileLoader() {
		this(new SQLParser());
	}
	
	public SQLFileLoader(SQLSchemaManager schemaManager) {
		this(new SQLParser(schemaManager));
	}
	
	public SQLFileLoader(SQLParser parser) {
		this.parser = parser;
	}

	public SQLSchemaManager getSchemaManager() {
		return parser.getSchemaManager();
	}


	public SQLParser getParser() {
		return parser;
	}

	public void setParser(SQLParser parser) {
		this.parser = parser;
	}

	public void load(File file) throws IOException {
		if (file.isDirectory()) {
			File[] array = file.listFiles();
			for (File child : array) {
				if (child.isDirectory() || child.getName().endsWith(".sql")) {
					load(child);
				}
			}
		} else {
			FileReader reader = new FileReader(file);
			parser.parseAll(reader);
		}
	}
	

}

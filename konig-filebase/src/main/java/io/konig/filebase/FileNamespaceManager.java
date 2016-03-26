package io.konig.filebase;

/*
 * #%L
 * Konig File Storage
 * %%
 * Copyright (C) 2015 Gregory McFall
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;

import org.openrdf.model.Namespace;

import io.konig.core.impl.MemoryNamespaceManager;

public class FileNamespaceManager extends MemoryNamespaceManager {

	private File file;

	public FileNamespaceManager(File file) {
		this.file = file;
	}
	
	public void load() throws IOException {
		if (file.exists()) {

			Properties properties = new Properties();
			FileReader reader = new FileReader(file);
			try {
				properties.load(reader);
				Enumeration<Object> keys = properties.keys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement().toString();
					String value = properties.getProperty(key);
					add(key, value);
				}
			} finally {
				reader.close();
			}
		}
		
	}
	
	public void save() throws IOException {
		
		FileWriter writer = new FileWriter(file);
		try {
			Collection<Namespace> set = byPrefix.values();
			Properties properties = new Properties();
			for (Namespace ns : set) {
				String key = ns.getPrefix();
				String value = ns.getName();
				properties.setProperty(key, value);
			}
			properties.store(writer, "Namespaces");
		} finally {
			writer.close();
		}
		
	}

}

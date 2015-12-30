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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import io.konig.core.io.ResourceFile;
import io.konig.core.io.ResourceManager;
import io.konig.core.io.impl.ResourceFileImpl;

public class FileManager implements ResourceManager {
	
	private File docRoot;

	public File getDocRoot() {
		return docRoot;
	}

	public void setDocRoot(File docRoot) {
		this.docRoot = docRoot;
	}

	public void delete(String contentLocation) {
		File entity = toFile(contentLocation);
		if (entity.exists()) {
			entity.delete();
			File metadata = metadata(entity);
			if (metadata.exists()) {
				metadata.delete();
			}
		}
		
	}
	
	public File metadata(File entity) {
		File dir = new File(entity.getParentFile(), ".metadata");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return new File(dir, entity.getName());
	}
	
	public File toFile(String url) {
		url = url.replaceAll(":", "/");
		url = url.replaceAll("/+", "/");
		
		return new File(docRoot, url);
	}

	public ResourceFile get(String contentLocation) throws IOException {
		ResourceFile result = null;
		File file = toFile(contentLocation);
		if (file.exists()) {
			byte[] buffer = new byte[(int)file.length()];
			FileInputStream input = new FileInputStream(file);
			try {
				input.read(buffer);
			} finally {
				input.close();
			}
			Properties properties = new Properties();
			File metadata = metadata(file);
			
			if (metadata.exists()) {
				input = new FileInputStream(metadata);
				try {
					properties.load(input);
				} finally {
					input.close();
				}
			}
			result = new ResourceFileImpl(buffer, properties);
		}
		return result;
	}

	public void put(ResourceFile file) throws IOException {
		
		File entity = toFile(file.getContentLocation());
		entity.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(entity);
		try {
			out.write(file.getEntityBody());
		} finally {
			out.close();
		}
		
		Properties p = null;
		if (file instanceof ResourceFileImpl) {
			p = ((ResourceFileImpl)file).getProperties();
		} else {
			p = new Properties();
			Enumeration<String> sequence = file.propertyNames();
			while (sequence.hasMoreElements()) {
				String key = sequence.nextElement();
				String value = file.getProperty(key);
				p.setProperty(key, value);
			}
		}
		
		File metadata = metadata(entity);
		out = new FileOutputStream(metadata);
		try {
			p.store(out, "Properties of " + file.getContentLocation());
		} finally {
			out.close();
		}
		
		
		
	}



}

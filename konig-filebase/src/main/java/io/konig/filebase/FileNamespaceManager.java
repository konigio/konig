package io.konig.filebase;

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
			Collection<Namespace> set = map.values();
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

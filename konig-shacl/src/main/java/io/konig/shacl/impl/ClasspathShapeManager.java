package io.konig.shacl.impl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.konig.core.KonigException;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.shacl.io.ShapeLoader;

public class ClasspathShapeManager extends MemoryShapeManager {
	private static final String RESOURCE_FOLDER = "konig/showl";
	private static ClasspathShapeManager INSTANCE;

	public ClasspathShapeManager()  {
		

	}
	
	public static ClasspathShapeManager instance() {
		if (INSTANCE==null) {
			INSTANCE = new ClasspathShapeManager();
			INSTANCE.load(RESOURCE_FOLDER);
		}
		return INSTANCE;
	}
	 
	
	public void load(String path) throws KonigException {

		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(null, this, nsManager);
		load(shapeLoader, path);
	}


	private void load(ShapeLoader loader, String path) {
		
		try {
			if (path.endsWith(".ttl")) {
				loadTurtle(loader, path);
			} else {
				path = path + "/";
				
				List<String> list = getResourceFiles(path);
				for (String resource : list) {
					load(loader, path+resource);
				}
			}
		} catch (IOException e) {
			throw new KonigException(e);
		}
		
	}


	private void loadTurtle(ShapeLoader loader, String path) {
		
		InputStream input = getResourceAsStream(path);
		try {
			loader.loadTurtle(input);
		} finally {
			close(input);
		}
		
	}


	private void close(Closeable input) {
		if (input != null) {
			try {
				input.close();
			} catch (IOException ignore) {
				
			}
		}
		
	}



	private List<String> getResourceFiles(String path) throws IOException {
		List<String> filenames = new ArrayList<>();

		try (InputStream in = getResourceAsStream(path);
				BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String resource;

			while ((resource = br.readLine()) != null) {
				filenames.add(resource);
			}
		}

		return filenames;
	}

	private InputStream getResourceAsStream(String resource) {
		final InputStream in = getContextClassLoader().getResourceAsStream(resource);

		return in == null ? getClass().getResourceAsStream(resource) : in;
	}

	private ClassLoader getContextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
}

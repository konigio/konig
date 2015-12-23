package io.konig.core.io.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.konig.core.io.ResourceFile;
import io.konig.core.io.ResourceManager;

public class MemoryResourceManager implements ResourceManager {
	private Map<String, ResourceFile> map = new HashMap<>();

	@Override
	public void delete(String contentLocation) throws IOException {
		map.remove(contentLocation);
	}

	@Override
	public ResourceFile get(String contentLocation) throws IOException {
		return map.get(contentLocation);
	}

	@Override
	public void put(ResourceFile file) throws IOException {
		map.put(file.getContentLocation(), file);
	}


}

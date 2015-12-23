package io.konig.core.io;

import java.io.IOException;

public interface ResourceManager {
	
	void delete(String contentLocation) throws IOException;
	ResourceFile get(String contentLocation) throws IOException;
	void put(ResourceFile file) throws IOException;

}

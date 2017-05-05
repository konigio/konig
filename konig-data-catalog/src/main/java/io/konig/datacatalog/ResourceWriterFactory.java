package io.konig.datacatalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.openrdf.model.URI;

public class ResourceWriterFactory implements WriterFactory {
	private File baseDir;
	
	public ResourceWriterFactory(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public PrintWriter createWriter(PageRequest request, URI resourceId) throws IOException, DataCatalogException {
		PathFactory factory = request.getBuildRequest().getPathFactory();
		String path = factory.pagePath(resourceId);
		
		File file = new File(baseDir, path);
		file.getParentFile().mkdirs();
		FileWriter fileWriter = new FileWriter(file);
		return new PrintWriter(fileWriter);
	}

}

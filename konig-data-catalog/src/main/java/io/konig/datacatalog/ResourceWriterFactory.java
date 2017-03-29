package io.konig.datacatalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

public class ResourceWriterFactory implements WriterFactory {
	private File baseDir;
	
	public ResourceWriterFactory(File baseDir) {
		this.baseDir = baseDir;
	}

	@Override
	public PrintWriter createWriter(PageRequest request, URI resourceId) throws IOException, DataCatalogException {
		Namespace ns = request.findNamespaceByName(resourceId.getNamespace());
		StringBuilder fileName = new StringBuilder();
		fileName.append(resourceId.getLocalName());
		fileName.append(".html");
		File shapeDir = new File(baseDir, ns.getPrefix());
		shapeDir.mkdirs();
		File file = new File(shapeDir, fileName.toString());
		FileWriter fileWriter = new FileWriter(file);
		return new PrintWriter(fileWriter);
	}

}

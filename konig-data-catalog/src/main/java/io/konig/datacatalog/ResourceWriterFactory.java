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
		File folder = new File(baseDir, ns.getPrefix());
		String folderName = request.folderName(resourceId);
		if (folderName != null) {
			folder = new File(folder, folderName);
		}
		
		folder.mkdirs();
		
		File file = new File(folder, fileName.toString());
		FileWriter fileWriter = new FileWriter(file);
		return new PrintWriter(fileWriter);
	}

}

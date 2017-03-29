package io.konig.datacatalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.NamespaceManager;

public class ClassIndexWriterFactory implements WriterFactory {

	private File baseDir;
	
	public ClassIndexWriterFactory(File baseDir) {

		this.baseDir = baseDir;
		baseDir.mkdirs();
	}

	@Override
	public PrintWriter createWriter(PageRequest request, URI resourceId) throws IOException, DataCatalogException {
		File file = null;
		if (resourceId == null) {
			file = new File(baseDir, "allclasses-index.html");
		} else {
			NamespaceManager nsManager = request.getGraph().getNamespaceManager();
			if (nsManager == null) {
				throw new DataCatalogException("NamespaceManager is not defined");
			}
			Namespace ns = request.findNamespaceByName(resourceId.stringValue());
			file = new File(baseDir, DataCatalogUtil.classIndexFileName(ns));
		}
		return new PrintWriter(new FileWriter(file));
	}

}

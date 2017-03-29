package io.konig.datacatalog;

import java.io.IOException;
import java.io.PrintWriter;

import org.openrdf.model.URI;

public interface WriterFactory {
	
	PrintWriter createWriter(PageRequest request, URI resourceId) throws IOException, DataCatalogException;

}

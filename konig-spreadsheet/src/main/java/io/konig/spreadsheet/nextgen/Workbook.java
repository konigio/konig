package io.konig.spreadsheet.nextgen;

import java.io.File;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class Workbook {
	private File file;
	private URI uri;

	public Workbook(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}
	
	public URI getURI() {
		if (uri == null) {
			uri = new URIImpl("urn:fileName:" + file.getName());
		}
		return uri;
	}

}

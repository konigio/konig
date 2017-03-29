package io.konig.datacatalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.openrdf.model.URI;

public class ShapeWriterFactory implements WriterFactory {
	private File shapeDir;
	
	public ShapeWriterFactory(File shapeDir) {
		this.shapeDir = shapeDir;
		shapeDir.mkdirs();
	}

	@Override
	public PrintWriter createWriter(PageRequest request, URI resourceId) throws IOException {
		File file = new File(shapeDir, resourceId.getLocalName());
		FileWriter fileWriter = new FileWriter(file);
		return new PrintWriter(fileWriter);
	}

}

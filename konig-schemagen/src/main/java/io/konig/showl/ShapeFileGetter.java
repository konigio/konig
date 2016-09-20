package io.konig.showl;

import java.io.File;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;

public class ShapeFileGetter implements FileGetter {
	private File baseDir;
	private NamespaceManager nsManager;
	
	

	public ShapeFileGetter(File baseDir, NamespaceManager nsManager) {
		this.baseDir = baseDir;
		this.nsManager = nsManager;
	}



	@Override
	public File getFile(URI shapeId) {
		
		
		Namespace n = nsManager.findByName(shapeId.getNamespace());
		if (n == null) {
			throw new KonigException("Prefix for namespace not found: " + shapeId.getNamespace());
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append(n.getPrefix());
		builder.append('_');
		builder.append(shapeId.getLocalName());
		builder.append(".ttl");
		
		
		return new File(baseDir, builder.toString());
	}

}

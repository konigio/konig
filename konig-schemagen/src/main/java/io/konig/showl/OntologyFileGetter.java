package io.konig.showl;

import java.io.File;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;

/**
 * A utility that gets the File that holds the description of a given ontology.
 * @author Greg McFall
 *
 */
public class OntologyFileGetter implements FileGetter {
	private File baseDir;
	private NamespaceManager nsManager;
	private String shapesOntologyFileName = "shapeNamespaces.ttl";
	
	public OntologyFileGetter(File baseDir, NamespaceManager nsManager) {
		this.baseDir = baseDir;
		this.nsManager = nsManager;
	}

	@Override
	public File getFile(URI ontologyId) {
		
		if (ontologyId.equals(OntologyWriter.SHAPE_NAMESPACE_FILE)) {
			return new File (baseDir, shapesOntologyFileName);
		}
		
		Namespace namespace = nsManager.findByName(ontologyId.stringValue());
		if (namespace == null) {
			throw new KonigException("Prefix not found for namespace: " + ontologyId.stringValue());
		}
		String prefix = namespace.getPrefix();
		StringBuilder builder = new StringBuilder();
		builder.append(prefix);
		builder.append(".ttl");
		return new File(baseDir, builder.toString());
	}
	

}

package io.konig.schemagen.gcp;


import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;

/**
 * A DatasetMapper that uses the preferred prefix for the namespace
 * of the target OWL class as the ID for the dataset.
 * @author Greg McFall
 *
 */
public class NamespaceDatasetMapper implements DatasetMapper {

	private NamespaceManager nsManager;
	
	

	public NamespaceDatasetMapper(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}



	@Override
	public String datasetForClass(Vertex owlClass) {
		
		Resource id = owlClass.getId();
		if (id instanceof URI) {
			URI uri = (URI) id;
			String name = uri.getNamespace();
			Namespace ns = nsManager.findByName(name);
			if (ns != null) {
				return ns.getPrefix();
			}
		}
		throw new KonigException("Namespace not found for class: " + id);
	}

}

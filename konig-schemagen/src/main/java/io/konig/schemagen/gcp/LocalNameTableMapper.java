package io.konig.schemagen.gcp;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class LocalNameTableMapper implements TableMapper {

	
	@Override
	public String tableForClass(Vertex owlClass) {
		
		Resource id = owlClass.getId();
		if (id instanceof URI) {
			URI uri = (URI) id;
			return uri.getLocalName();
		}
		
		return null;
	}

}

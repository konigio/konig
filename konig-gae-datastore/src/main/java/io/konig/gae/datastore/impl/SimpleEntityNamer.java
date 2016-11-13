package io.konig.gae.datastore.impl;

import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.gae.datastore.EntityNamer;
import io.konig.shacl.Shape;

/**
 * An EntityNamer that uses the local name of the OWL Class as the Entity name.
 * @author Greg McFall
 *
 */
public class SimpleEntityNamer implements EntityNamer {


	@Override
	public String entityName(URI owlClass) {
		return owlClass.getLocalName();
	}

}

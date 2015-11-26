package io.konig.shacl.context;

import org.openrdf.model.URI;

/**
 * Assigns a name to the JSON-LD context for a data shape.
 * @author Greg McFall
 *
 */
public interface ContextNamer {

	URI forShape(URI shapeId);
}

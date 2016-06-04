package io.konig.datagen;

import org.openrdf.model.URI;

import io.konig.shacl.Shape;

/**
 * A generator that creates IRI values for individual instances of a given shape.
 * @author Greg McFall
 *
 */
public interface IriGenerator {

	URI createIRI(URI owlClass, int index);
}

package io.konig.schemagen.gcp;

import io.konig.core.Vertex;

/**
 * An interface that yields the appropriate Google Cloud Platform Project
 * for a given OWL class.
 * 
 * @author Greg McFall
 *
 */
public interface ProjectMapper {

	String projectForClass(Vertex owlClass);
}

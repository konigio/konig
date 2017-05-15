package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintable;

/**
 * A rule for generating a property for some resource.
 * @author Greg McFall
 *
 */
public interface PropertyRule extends PrettyPrintable {

	URI getPredicate();
	
}

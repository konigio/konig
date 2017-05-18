package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintable;
import io.konig.shacl.Shape;

/**
 * A rule for generating a property for some resource.
 * @author Greg McFall
 *
 */
public interface PropertyRule extends PrettyPrintable {

	/**
	 * The ShapeRule within which this PropertyRule is contained.
	 * @return
	 */
	ShapeRule getContainer();
	
	void setContainer(ShapeRule container);
	
	/**
	 * The Shape from which the property is accessed
	 */
	DataChannel getDataChannel();
	
	ShapeRule getNestedRule();
	
	/**
	 * The predicate for property described by this Rule.
	 */
	URI getPredicate();
	
}

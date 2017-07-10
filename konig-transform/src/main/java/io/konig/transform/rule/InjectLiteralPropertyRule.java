package io.konig.transform.rule;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;

/**
 * A rule which asserts that a given property has a fixed, literal value that 
 * must be injected at runtime.
 * @author Greg McFall
 *
 */
public class InjectLiteralPropertyRule extends LiteralPropertyRule {

	public InjectLiteralPropertyRule(DataChannel channel, URI predicate, Literal value) {
		super(channel, predicate, value);
	}
	
	

}

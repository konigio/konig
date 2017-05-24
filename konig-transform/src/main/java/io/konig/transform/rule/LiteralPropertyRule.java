package io.konig.transform.rule;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;

/**
 * A rule which asserts that a given property has a fixed, literal value.
 * @author Greg McFall
 *
 */
public class LiteralPropertyRule extends AbstractPropertyRule {
	
	private URI predicate;
	private Literal value;

	public LiteralPropertyRule(DataChannel channel, URI predicate, Literal value) {
		super(channel);
		this.predicate = predicate;
		this.value = value;
	}

	@Override
	public URI getPredicate() {
		return predicate;
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		out.field("value", value);
	}

	public Literal getValue() {
		return value;
	}

}

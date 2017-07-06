package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;

public class NullPropertyRule extends AbstractPropertyRule {

	private URI predicate;
	
	public NullPropertyRule(DataChannel channel, URI predicate) {
		super(channel);
		this.predicate = predicate;
	}

	@Override
	public URI getPredicate() {
		return predicate;
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		// Do nothing
	}

}

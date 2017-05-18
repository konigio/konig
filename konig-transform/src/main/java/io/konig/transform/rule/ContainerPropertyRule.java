package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;

public class ContainerPropertyRule extends AbstractPropertyRule {
	private URI predicate;

	public ContainerPropertyRule(URI predicate, DataChannel channel) {
		super(channel);
		this.predicate = predicate;
	}

	@Override
	public URI getPredicate() {
		return predicate;
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {

	}

}

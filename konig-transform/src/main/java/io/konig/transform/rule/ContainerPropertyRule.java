package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;

/**
 * The rule used for a property that has a nested shape.
 * @author Greg McFall
 *
 */
public class ContainerPropertyRule extends AbstractPropertyRule {
	private URI predicate;

	// TODO: eliminate the DataChannel property.  DataChannels should be defined only on the nested shape.
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

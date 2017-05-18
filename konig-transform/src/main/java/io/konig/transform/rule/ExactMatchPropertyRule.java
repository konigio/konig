package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;

public class ExactMatchPropertyRule extends AbstractPropertyRule  {
	
	private URI predicate;

	public ExactMatchPropertyRule(DataChannel sourceShape, URI predicate) {
		super(sourceShape);
		
		this.predicate = predicate;
	}

	@Override
	public URI getPredicate() {
		return predicate;
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		out.field("predicate", predicate);
	}
}

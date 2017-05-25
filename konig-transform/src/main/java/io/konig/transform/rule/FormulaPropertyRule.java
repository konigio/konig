package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;

public class FormulaPropertyRule extends AbstractPropertyRule {
	
	private URI targetPredicate;
	private PropertyConstraint sourceProperty;

	public FormulaPropertyRule(DataChannel channel, URI targetPredicate, PropertyConstraint sourceProperty) {
		super(channel);
		this.targetPredicate = targetPredicate;
		this.sourceProperty = sourceProperty;
	}

	@Override
	public URI getPredicate() {
		return targetPredicate;
	}

	public PropertyConstraint getSourceProperty() {
		return sourceProperty;
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		out.beginObject(sourceProperty);
		out.field("predicate", sourceProperty.getPredicate());
		out.field("formula", sourceProperty.getFormula().getText());
		out.endObject();
		
	}

}

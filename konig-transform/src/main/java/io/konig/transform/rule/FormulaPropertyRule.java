package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;

public class FormulaPropertyRule extends AbstractPropertyRule {

	private PropertyConstraint targetProperty;
	private PropertyConstraint sourceProperty;

	public FormulaPropertyRule(DataChannel channel, PropertyConstraint targetProperty, PropertyConstraint sourceProperty) {
		super(channel);
		this.targetProperty = targetProperty;
		this.sourceProperty = sourceProperty;
	}

	@Override
	public URI getPredicate() {
		return targetProperty.getPredicate();
	}

	public PropertyConstraint getSourceProperty() {
		return sourceProperty;
	}

	public PropertyConstraint getTargetProperty() {
		return targetProperty;
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		out.beginObject(sourceProperty);
		out.field("predicate", sourceProperty.getPredicate());
		out.field("formula", sourceProperty.getFormula().getText());
		out.endObject();

	}

}

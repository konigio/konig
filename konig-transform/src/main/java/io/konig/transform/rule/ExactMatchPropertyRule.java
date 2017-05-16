package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;

public class ExactMatchPropertyRule extends AbstractPropertyRule implements PropertyRule {
	
	private URI predicate;

	public ExactMatchPropertyRule(RankedVariable<Shape> sourceShape, URI predicate) {
		this.sourceShape = sourceShape;
		this.predicate = predicate;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.beginObjectField("sourceShape", sourceShape);
		
		out.endObjectField(sourceShape);
		out.field("name", sourceShape.getName());
		out.field("value", sourceShape.getValue().getId());
		out.field("predicate", predicate);
		out.endObject();
		

	}

	@Override
	public URI getFocusPredicate() {
		return predicate;
	}
}

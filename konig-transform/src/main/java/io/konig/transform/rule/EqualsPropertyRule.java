package io.konig.transform.rule;

import org.openrdf.model.URI;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A rule for generating the value of a property by copying the value from some source entity.
 * @author Greg McFall
 *
 */
public class EqualsPropertyRule extends AbstractPrettyPrintable implements PropertyRule {

	private Variable<Shape> sourceEntity;
	private PropertyConstraint property;
	
	public EqualsPropertyRule(Variable<Shape> sourceShape, PropertyConstraint property) {
		this.sourceEntity = sourceShape;
		this.property = property;
	}

	public Variable<Shape> getSourceEntity() {
		return sourceEntity;
	}

	@Override
	public URI getPredicate() {
		return property.getPredicate();
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.beginObject(this);
		out.field("sourceEntity.name", sourceEntity.getName());
		out.field("property.predicate", property.getPredicate());
		out.endObject();
		
	}
	
	
}

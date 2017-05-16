package io.konig.transform.rule;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.shacl.Shape;

public abstract class AbstractPropertyRule extends AbstractPrettyPrintable implements PropertyRule {
	
	protected ShapeRule container;
	protected RankedVariable<Shape> sourceShape;
	protected ShapeRule nestedRule;

	
	@Override
	public ShapeRule getContainer() {
		return container;
	}
	
	@Override
	public void setContainer(ShapeRule container) {
		this.container = container;
	}

	@Override
	public RankedVariable<Shape> getSourceShapeVariable() {
		return sourceShape;
	}

	@Override
	public ShapeRule getNestedRule() {
		return nestedRule;
	}

	public void setNestedRule(ShapeRule nestedRule) {
		this.nestedRule = nestedRule;
	}


}

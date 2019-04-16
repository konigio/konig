package io.konig.core.showl;

import java.util.Set;

/**
 * An expression that evaluates to a single source property.
 * This expression is used to describe a one-to-one mapping between a source property 
 * and a target property.
 * @author Greg McFall
 *
 */
public abstract class ShowlPropertyExpression implements ShowlExpression {

	private ShowlPropertyShape sourceProperty;

	protected ShowlPropertyExpression(ShowlPropertyShape sourceProperty) {
		this.sourceProperty = sourceProperty;
	}
	
	public static ShowlPropertyExpression from(ShowlPropertyShape p) {
		if (p instanceof ShowlDirectPropertyShape) {
			return new ShowlDirectPropertyExpression((ShowlDirectPropertyShape)p);
		} else {
			return new ShowlDerivedPropertyExpression((ShowlDerivedPropertyShape) p);
		}
	}

	/**
	 * Get the source property
	 */
	public ShowlPropertyShape getSourceProperty() {
		return sourceProperty;
	}

	@Override
	public ShowlNodeShape rootNode() {
		return sourceProperty.getRootNode();
	}

	@Override
	public String displayValue() {
		return sourceProperty.getPath();
	}
	
	public String toString() {
		return "ShowlPropertyExpression(" + sourceProperty.getPath() + ")";
	}

	@Override
	public void addRequiredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set) {
		set.add(sourceProperty);
	}
	
	

}

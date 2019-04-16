package io.konig.core.showl;

public class ShowlDerivedPropertyExpression extends ShowlPropertyExpression {

	public ShowlDerivedPropertyExpression(ShowlDerivedPropertyShape sourceProperty) {
		super(sourceProperty);
	}

	public ShowlDerivedPropertyShape getSourceProperty() {
		return (ShowlDerivedPropertyShape) super.getSourceProperty();
	}
}

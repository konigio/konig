package io.konig.core.showl;

public class ShowlDirectPropertyExpression extends ShowlPropertyExpression {

	public ShowlDirectPropertyExpression(ShowlDirectPropertyShape sourceProperty) {
		super(sourceProperty);
	}

	public ShowlDirectPropertyShape getSourceProperty() {
		return (ShowlDirectPropertyShape) super.getSourceProperty();
	}

}

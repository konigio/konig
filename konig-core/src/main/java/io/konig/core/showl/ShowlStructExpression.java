package io.konig.core.showl;

import java.util.Set;

public class ShowlStructExpression implements ShowlExpression {
	
	private ShowlDirectPropertyShape propertyShape;
	
	public ShowlStructExpression(ShowlDirectPropertyShape propertyShape) {
		this.propertyShape = propertyShape;
	}

	@Override
	public String displayValue() {
		return propertyShape.getPath();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		for (ShowlPropertyShape p : propertyShape.getValueShape().getProperties()) {
			if (p.getSelectedExpression() != null) {
				p.getSelectedExpression().addDeclaredProperties(sourceNodeShape, set);
			}
		}
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		for (ShowlPropertyShape p : propertyShape.getValueShape().getProperties()) {
			if (p.getSelectedExpression() != null) {
				p.getSelectedExpression().addProperties(set);
			}
		}
		
	}

}

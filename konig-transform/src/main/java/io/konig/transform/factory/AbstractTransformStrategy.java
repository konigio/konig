package io.konig.transform.factory;

abstract public class AbstractTransformStrategy implements TransformStrategy {
	protected ShapeRuleFactory factory;
	

	@Override
	public void init(ShapeRuleFactory factory) {
		this.factory = factory;
	}

}

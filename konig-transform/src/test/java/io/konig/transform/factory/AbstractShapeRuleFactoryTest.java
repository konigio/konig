package io.konig.transform.factory;

import static org.junit.Assert.assertTrue;

import org.openrdf.model.URI;

import io.konig.shacl.Shape;
import io.konig.transform.rule.ShapeRule;

public class AbstractShapeRuleFactoryTest extends TransformTest {

	protected ShapeRuleFactory shapeRuleFactory = new ShapeRuleFactory(shapeManager);


	
	protected void useBigQueryTransformStrategy() {
		shapeRuleFactory.setStrategy(new BigQueryTransformStrategy());
		
	}
	
	protected ShapeRule createShapeRule(URI shapeId) throws Exception {
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		return shapeRuleFactory.createShapeRule(shape);
	}
}
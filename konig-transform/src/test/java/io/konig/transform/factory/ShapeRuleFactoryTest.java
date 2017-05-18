package io.konig.transform.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.transform.factory.ShapeRuleFactory;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RenamePropertyRule;
import io.konig.transform.rule.ShapeRule;

public class ShapeRuleFactoryTest extends TransformTest {
	
	private ShapeRuleFactory shapeRuleFactory = new ShapeRuleFactory(shapeManager);

	@Test
	public void testFlattenedField() throws Exception {

		load("src/test/resources/konig-transform/flattened-field");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
	
		

		ShapeRule shapeRule = createShapeRule(shapeId);
		assertTrue(shapeRule != null);
		
		List<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		
		PropertyRule rule = propertyList.get(0);

		assertEquals(Schema.address, rule.getPredicate());
		
		ShapeRule nestedRule = rule.getNestedRule();
		assertTrue(nestedRule != null);
		
		propertyList = nestedRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		rule = propertyList.get(0);
		assertEquals(Schema.postalCode, rule.getPredicate());
		
	
	}
	
	@Test
	public void testExactMatchProperty() throws Exception {

		load("src/test/resources/konig-transform/field-exact-match");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		assertTrue(shapeRule != null);
		
		List<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		
		PropertyRule rule = propertyList.get(0);
		assertTrue(rule instanceof ExactMatchPropertyRule);

		assertEquals(Schema.givenName, rule.getPredicate());
	
	}
	
	@Test
	public void testRenameProperty() throws Exception {
		load("src/test/resources/konig-transform/rename-fields");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		
		ShapeRule shapeRule = createShapeRule(shapeId);
		assertTrue(shapeRule != null);
		
		List<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		
		PropertyRule rule = shapeRule.propertyRule(Schema.givenName);
		assertTrue(rule != null);
		assertTrue(rule instanceof RenamePropertyRule);
		RenamePropertyRule renameRule = (RenamePropertyRule) rule;
		assertEquals(0, renameRule.getPathIndex());
		assertEquals("first_name", renameRule.getSourceProperty().getPredicate().getLocalName());
		
	}

	private ShapeRule createShapeRule(URI shapeId) throws Exception {
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		return shapeRuleFactory.createShapeRule(shape);
	}

}

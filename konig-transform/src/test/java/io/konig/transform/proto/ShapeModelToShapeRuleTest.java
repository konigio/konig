package io.konig.transform.proto;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.factory.TransformTest;
import io.konig.transform.rule.BinaryBooleanExpression;
import io.konig.transform.rule.BooleanExpression;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.FromItem;
import io.konig.transform.rule.JoinRule;
import io.konig.transform.rule.NullPropertyRule;
import io.konig.transform.rule.PropertyComparison;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RenamePropertyRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.rule.TransformBinaryOperator;

public class ShapeModelToShapeRuleTest extends TransformTest {
	
	private ShapeModelToShapeRule factory = new ShapeModelToShapeRule();
	
	@Test
	public void testNullValue() throws Exception {
		

		factory.setFailIfPropertyNotMapped(false);
		load("src/test/resources/konig-transform/bigquery-transform-null");
		URI shapeId = iri("http://example.com/shapes/PersonShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		assertTrue(shapeRule != null);
		
		PropertyRule propertyRule = shapeRule.getProperty(Schema.name);
		assertTrue(propertyRule instanceof NullPropertyRule);
		
	}
	
	@Test
	public void testJoinNestedEntity() throws Exception {

		load("src/test/resources/konig-transform/join-nested-entity");
		URI memberShapeId = iri("http://example.com/shapes/MemberShape");
		URI originOrgShapeId = iri("http://example.com/shapes/OriginOrganizationShape");
		URI originPersonShapeId = iri("http://example.com/shapes/OriginPersonShape");

		ShapeRule shapeRule = createShapeRule(memberShapeId);
		assertTrue(shapeRule != null);
		
		PropertyRule memberOf = shapeRule.getProperty(Schema.memberOf);
		assertTrue(memberOf != null);
		
		ShapeRule nested = memberOf.getNestedRule();
		assertTrue(nested != null);
		assertEquals(originOrgShapeId, nested.getTargetShape().getId());
	
		FromItem fromItem = shapeRule.getFromItem();
		
		assertTrue(fromItem instanceof JoinRule);
		JoinRule join = (JoinRule) fromItem;
		
		assertEquals(originPersonShapeId, join.getLeftChannel().getShape().getId());
		
		assertEquals(originOrgShapeId, join.getRightChannel().getShape().getId());
		
		BooleanExpression condition = join.getCondition();
		assertTrue(condition instanceof PropertyComparison);
		PropertyComparison binary = (PropertyComparison) condition;
		assertEquals(TransformBinaryOperator.EQUAL, binary.getOperator());
		
		assertEquals(originOrgShapeId, binary.getRight().getChannel().getShape().getId());
		assertEquals(Konig.id, binary.getRight().getPredicate());
		
		assertEquals(originPersonShapeId, binary.getLeft().getChannel().getShape().getId());
		assertEquals(Schema.memberOf, binary.getLeft().getPredicate());
		
	}
	
	@Test
	public void testJoinNestedEntityByPk() throws Exception {
		

		load("src/test/resources/konig-transform/join-nested-entity-by-pk");
		URI memberShapeId = iri("http://example.com/shapes/TargetMemberShape");

		ShapeRule shapeRule = createShapeRule(memberShapeId);
		assertTrue(shapeRule != null);
		
		PropertyRule memberOf = shapeRule.getProperty(Schema.memberOf);
		assertTrue(memberOf != null);
		
		
	}
	
	@Test
	public void testJoinById() throws Exception {

		load("src/test/resources/konig-transform/join-by-id");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		assertTrue(shapeRule != null);
		
		Collection<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(2, propertyList.size());
		
		assertTrue(shapeRule.getProperty(Schema.givenName) != null);
		assertTrue(shapeRule.getProperty(Schema.alumniOf) != null);
		
		
		FromItem fromItem = shapeRule.getFromItem();
		assertTrue(fromItem instanceof JoinRule);
		
		JoinRule join = (JoinRule) fromItem;
		
		assertTrue(join.getLeft() instanceof DataChannel);
		assertTrue(join.getRight() instanceof DataChannel);
		
		DataChannel a = (DataChannel) join.getLeft();
		DataChannel b = (DataChannel) join.getRight();
		
		
		assertEquals("a", a.getName());
		assertEquals("b", b.getName());
		
		
		assertTrue(join.getCondition() instanceof PropertyComparison);
		PropertyComparison condition = (PropertyComparison) join.getCondition();
		
		assertEquals(TransformBinaryOperator.EQUAL, condition.getOperator());
		assertEquals(Konig.id, condition.getLeft().getPredicate());
		assertEquals(Konig.id, condition.getRight().getPredicate());
	
	}
	
	@Test
	public void testFlattenedField() throws Exception {

		load("src/test/resources/konig-transform/flattened-field");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
	
		

		ShapeRule shapeRule = createShapeRule(shapeId);
		assertTrue(shapeRule != null);
		
		Collection<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		
		PropertyRule rule = propertyList.iterator().next();

		assertEquals(Schema.address, rule.getPredicate());
		
		ShapeRule nestedRule = rule.getNestedRule();
		assertTrue(nestedRule != null);
		
		propertyList = nestedRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		rule = propertyList.iterator().next();
		assertEquals(Schema.postalCode, rule.getPredicate());
		
	
	}
	
	@Test
	public void testRenameProperty() throws Exception {
		load("src/test/resources/konig-transform/rename-fields");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");
		
		ShapeRule shapeRule = createShapeRule(shapeId);
		assertTrue(shapeRule != null);
		
		Collection<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		
		PropertyRule rule = shapeRule.getProperty(Schema.givenName);
		assertTrue(rule != null);
		assertTrue(rule instanceof RenamePropertyRule);
		RenamePropertyRule renameRule = (RenamePropertyRule) rule;
		assertEquals(0, renameRule.getPathIndex());
		assertEquals("first_name", renameRule.getSourceProperty().getPredicate().getLocalName());
		
	}

	@Test
	public void testExactMatchProperty() throws Exception {

		load("src/test/resources/konig-transform/field-exact-match");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		assertTrue(shapeRule != null);
		
		Collection<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(1, propertyList.size());
		
		PropertyRule rule = propertyList.iterator().next();
		assertTrue(rule instanceof ExactMatchPropertyRule);

		assertEquals(Schema.givenName, rule.getPredicate());
	
	}
	
	private ShapeRule createShapeRule(URI shapeId) throws ShapeTransformException {
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		
		ShapeModelFactory shapeModelFactory = new ShapeModelFactory(shapeManager, null, new OwlReasoner(graph));
		ShapeModel shapeModel = shapeModelFactory.createShapeModel(shape);
		
		return factory.toShapeRule(shapeModel);
	}

	
	


}

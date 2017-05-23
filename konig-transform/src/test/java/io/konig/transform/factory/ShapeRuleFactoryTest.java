package io.konig.transform.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.Shape;
import io.konig.transform.rule.BinaryBooleanExpression;
import io.konig.transform.rule.BooleanExpression;
import io.konig.transform.rule.TransformBinaryOperator;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.JoinStatement;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RenamePropertyRule;
import io.konig.transform.rule.ShapeRule;

public class ShapeRuleFactoryTest extends AbstractShapeRuleFactoryTest {
	
	
	@Test
	public void testJoinNestedEntityByPk() throws Exception {
		
		useBigQueryTransformStrategy();

		load("src/test/resources/konig-transform/join-nested-entity-by-pk");
		URI memberShapeId = iri("http://example.com/shapes/TargetMemberShape");

		ShapeRule shapeRule = createShapeRule(memberShapeId);
		assertTrue(shapeRule != null);
		
		PropertyRule memberOf = shapeRule.getProperty(Schema.memberOf);
		assertTrue(memberOf != null);
		
		
	}

	@Ignore
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
	
		List<DataChannel> channels = nested.getChannels();
		assertEquals(1, channels.size());
		
		DataChannel channel = channels.get(0);
		JoinStatement join = channel.getJoinStatement();
		assertTrue(join != null);
		
		assertEquals(originPersonShapeId, join.getLeft().getShape().getId());
		assertEquals(originOrgShapeId, join.getRight().getShape().getId());
		
		BooleanExpression condition = join.getCondition();
		assertTrue(condition instanceof BinaryBooleanExpression);
		BinaryBooleanExpression binary = (BinaryBooleanExpression) condition;
		assertEquals(TransformBinaryOperator.EQUAL, binary.getOperator());
		assertEquals(Schema.memberOf, binary.getLeftPredicate());
		assertEquals(Konig.id, binary.getRightPredicate());
	}
	
	@Ignore
	public void testJoinById() throws Exception {

		load("src/test/resources/konig-transform/join-by-id");
		URI shapeId = iri("http://example.com/shapes/BqPersonShape");

		ShapeRule shapeRule = createShapeRule(shapeId);
		assertTrue(shapeRule != null);
		
		Collection<PropertyRule> propertyList = shapeRule.getPropertyRules();
		assertEquals(2, propertyList.size());
		
		assertTrue(shapeRule.getProperty(Schema.givenName) != null);
		assertTrue(shapeRule.getProperty(Schema.alumniOf) != null);
		
		List<DataChannel> channelList = shapeRule.getChannels();
		Collections.sort(channelList);
		assertEquals(2, channelList.size());
		
		DataChannel a = channelList.get(0);
		DataChannel b = channelList.get(1);
		
		assertEquals("a", a.getName());
		assertEquals("b", b.getName());
		
		assertTrue(a.getJoinStatement()==null);
		
		JoinStatement join = b.getJoinStatement();
		
		assertTrue(join != null);
		assertTrue(join.getCondition() instanceof BinaryBooleanExpression);
		BinaryBooleanExpression condition = (BinaryBooleanExpression) join.getCondition();
		
		assertEquals(TransformBinaryOperator.EQUAL, condition.getOperator());
		assertEquals(Konig.id, condition.getLeftPredicate());
		assertEquals(Konig.id, condition.getRightPredicate());
		
		
	
	}
	
	@Ignore
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
	
	@Ignore
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
	
	@Ignore
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

}

package io.konig.transform.factory;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;

import io.konig.core.vocab.Schema;
import io.konig.shacl.Shape;
import io.konig.transform.factory.SourceProperty;
import io.konig.transform.factory.SourceShape;
import io.konig.transform.factory.TargetProperty;
import io.konig.transform.factory.TargetShape;

public class TargetShapeTest extends TransformTest {

	@Test
	public void testMatch() throws Exception {

		load("src/test/resources/konig-transform/org-founder-zipcode");
		URI targetShapeId = iri("http://example.com/shapes/BqOrganizationShape");
		URI sourceShapeId = iri("http://example.com/shapes/OriginOrganizationShape");
		
		Shape targetShape = shapeManager.getShapeById(targetShapeId);
		Shape sourceShape = shapeManager.getShapeById(sourceShapeId);
		
		TargetShape targetNode = TargetShape.create(targetShape);
		SourceShape sourceNode = SourceShape.create(sourceShape);
		
		targetNode.match(sourceNode);
		
		TargetProperty founder = targetNode.getProperty(Schema.founder);
		assertTrue(founder != null);
		
		TargetProperty address = founder.getNestedShape().getProperty(Schema.address);
		assertTrue(address != null);
		
		TargetProperty postalCode = address.getNestedShape().getProperty(Schema.postalCode);
		assertTrue(postalCode != null);
		
		Set<SourceProperty> matchSet = postalCode.getMatches();
		assertEquals(1, matchSet.size());
		
		SourceProperty zipCode = matchSet.iterator().next();
		assertEquals("http://example.com/ns/alias/founder_zipcode", zipCode.getPropertyConstraint().getPredicate().stringValue());
		
	}

}

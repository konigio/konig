package io.konig.dao.core;

import static org.junit.Assert.*;

import org.junit.Test;

public class ShapeQueryTest {

	@Test
	public void test() {
		
		String shapeId = "http://example.com/shape/PersonShape";
		
		ShapeQuery query = ShapeQuery.newBuilder()
			.setShapeId(shapeId)
			.beginPredicateConstraint()
				.setPropertyName("givenName")
				.setOperator(ConstraintOperator.EQUAL)
				.setValue("Alice")
			.endPredicateConstraint()
			.build();
		
		assertEquals(query.getShapeId(), shapeId);
		
		ShapeFilter filter = query.getFilter();
		assertTrue(filter instanceof  PredicateConstraint);
		PredicateConstraint constraint = (PredicateConstraint) filter;
		assertEquals("givenName", constraint.getPropertyName());
		assertEquals(ConstraintOperator.EQUAL, constraint.getOperator());
		assertEquals("Alice", constraint.getValue());
	}

}

package io.konig.dao.core;

/*
 * #%L
 * Konig DAO Core
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

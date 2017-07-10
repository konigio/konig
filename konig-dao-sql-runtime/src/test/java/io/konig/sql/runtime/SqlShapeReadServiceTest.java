package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO SQL Runtime
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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import java.io.Writer;

import org.junit.Test;

import io.konig.dao.core.ConstraintOperator;
import io.konig.dao.core.DaoException;
import io.konig.dao.core.Format;
import io.konig.dao.core.ShapeQuery;

public class SqlShapeReadServiceTest {

	@Test
	public void test() throws Exception {
		String shapeId = "http://example.com/shape/PersonShape";
		
		EntityStructureService structService = mock(EntityStructureService.class);
		
		when(structService.structureOfShape(shapeId)).thenReturn(new EntityStructure("schema.Person"));

		String expected = 
				"SELECT * FROM schema.Person\n" + 
				"WHERE givenName = \"Alice\"";
		
		
		MockSqlShapeReadService mock = new MockSqlShapeReadService(structService, expected);
		
		ShapeQuery query = new ShapeQuery.Builder()
			.setShapeId(shapeId)
			.beginPredicateConstraint()
				.setPropertyName("givenName")
				.setOperator(ConstraintOperator.EQUAL)
				.setValue("Alice")
			.endPredicateConstraint()
			.build();
		
		
		mock.execute(query, null, null);
	}
	
	private static class MockSqlShapeReadService extends SqlShapeReadService {
		private String expectedSQL;

		public MockSqlShapeReadService(EntityStructureService service, String expectedSQL) {
			super(service);
			this.expectedSQL = expectedSQL;
		}

		@Override
		protected void executeSql(EntityStructure struct, String sql, Writer output, Format format) throws DaoException {
			
			assertEquals(expectedSQL, sql);
			
		}
		
	}

}

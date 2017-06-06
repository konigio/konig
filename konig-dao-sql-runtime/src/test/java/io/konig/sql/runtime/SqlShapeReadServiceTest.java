package io.konig.sql.runtime;

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
		
		TableNameService tableNameService = mock(TableNameService.class);
		when(tableNameService.tableName(shapeId)).thenReturn("schema.Person");

		String expected = 
				"SELECT * FROM schema.Person\n" + 
				"WHERE givenName = \"Alice\"";
		MockSqlShapeReadService mock = new MockSqlShapeReadService(tableNameService, expected);
		
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

		public MockSqlShapeReadService(TableNameService tableNameService, String expectedSQL) {
			super(tableNameService);
			this.expectedSQL = expectedSQL;
		}

		@Override
		protected void executeSql(String sql, Writer output, Format format) throws DaoException {
			
			assertEquals(expectedSQL, sql);
			
		}
		
	}

}

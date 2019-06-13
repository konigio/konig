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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Writer;

import org.junit.Test;
import org.openrdf.model.impl.URIImpl;

import io.konig.dao.core.ChartSeriesFactory;
import io.konig.dao.core.ConstraintOperator;
import io.konig.dao.core.DaoException;
import io.konig.dao.core.Format;
import io.konig.dao.core.ShapeQuery;

public class SqlShapeReadServiceTest {

	@Test
	public void test() throws Exception {
		String shapeId = "http://example.com/shape/PersonShape";
		
		EntityStructureService structService = mock(EntityStructureService.class);
		
		EntityStructure struct = new EntityStructure("schema.Person");	
		
		FieldInfo givenName = new FieldInfo();
		givenName.setName("givenName");		
		givenName.setFieldType(new URIImpl("http://www.w3.org/2001/XMLSchema#string"));
		struct.addField(givenName);
		
		when(structService.structureOfShape(shapeId)).thenReturn(struct);

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
	
	@Test
	public void testStartAssessment() throws Exception {
		String shapeId = "http://example.com/shape/StartAssessmentUniqueCountByGradeShape";
		
		EntityStructure struct = new EntityStructure("schema.StartAssessmentUniqueCountByGrade");	
		
		FieldInfo country = new FieldInfo();
		country.setName("country");		
		country.setFieldType(new URIImpl("http://www.w3.org/2001/XMLSchema#string"));
		struct.addField(country);
		
		EntityStructure timeIntervalStruct = new EntityStructure("timeInterval");
		
		FieldInfo durationUnit = new FieldInfo();				
		durationUnit.setName("durationUnit");
		durationUnit.setFieldType(new URIImpl("http://www.w3.org/2001/XMLSchema#string"));		
		
		FieldInfo intervalStart = new FieldInfo();
		intervalStart.setName("intervalStart");
		intervalStart.setFieldType(new URIImpl("http://www.w3.org/2001/XMLSchema#dateTime"));		
		
		
		EntityStructure sampleStruct = new EntityStructure("sampleStruct");
		FieldInfo sampleField = new FieldInfo();
		sampleField.setFieldType(new URIImpl("http://www.w3.org/2001/XMLSchema#long"));
		sampleField.setName("timeStamp");
		sampleStruct.addField(sampleField);			
		timeIntervalStruct.addField(durationUnit);	
		timeIntervalStruct.addField(intervalStart);			
		timeIntervalStruct.addField("sampleStruct",sampleStruct);		
		struct.addField("timeInterval",timeIntervalStruct);		
		FieldInfo uniqueCount = new FieldInfo();
		uniqueCount.setName("uniqueCount");		
		uniqueCount.setFieldType(new URIImpl("http://www.w3.org/2001/XMLSchema#int"));
		struct.addField(uniqueCount);
		
		EntityStructureService structService = mock(EntityStructureService.class);
		
		when(structService.structureOfShape(shapeId)).thenReturn(struct);
		
		String expected = 
				"SELECT * FROM schema.StartAssessmentUniqueCountByGrade\n" +
				"WHERE\n" +
				    "    country = \"https://pearsonmypedia.com/country/India\" AND\n" +
				    "    timeInterval.durationUnit = \"http://www.w3.org/2006/time#unitMonth\" AND\n" +
				    "    timeInterval.intervalStart >= \"2017-04-01\" AND\n" +
				    "    timeInterval.intervalStart <= \"2017-06-01\" AND\n" +
				    "    uniqueCount = 1 AND\n" +
				    "    timeInterval.intervalStart.sampleStruct.timeStamp = 21392138923";
		
		
		MockSqlShapeReadService mock = new MockSqlShapeReadService(structService, expected);
		
		ShapeQuery query = new ShapeQuery.Builder()
			.setShapeId(shapeId)
			.beginPredicateConstraint()
				.setPropertyName("country")
				.setOperator(ConstraintOperator.EQUAL)
				.setValue("https://pearsonmypedia.com/country/India")
			.endPredicateConstraint()
			.beginPredicateConstraint()
				.setPropertyName("timeInterval.durationUnit")
				.setOperator(ConstraintOperator.EQUAL)
				.setValue("http://www.w3.org/2006/time#unitMonth")
			.endPredicateConstraint()
			.beginPredicateConstraint()
				.setPropertyName("timeInterval.intervalStart")
				.setOperator(ConstraintOperator.GREATER_THAN_OR_EQUAL)
				.setValue("2017-04-01")
			.endPredicateConstraint()
			.beginPredicateConstraint()
				.setPropertyName("timeInterval.intervalStart")
				.setOperator(ConstraintOperator.LESS_THAN_OR_EQUAL)
				.setValue("2017-06-01")
			.endPredicateConstraint()
			.beginPredicateConstraint()
				.setPropertyName("uniqueCount")
				.setOperator(ConstraintOperator.EQUAL)
				.setValue("1")
			.endPredicateConstraint()
			.beginPredicateConstraint()
				.setPropertyName("timeInterval.intervalStart.sampleStruct.timeStamp")
				.setOperator(ConstraintOperator.EQUAL)
				.setValue("21392138923")
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
		protected void executeSql(EntityStructure struct, ShapeQuery query, Writer output, Format format) throws DaoException {
			String sql = toSql(struct, query);
			assertEquals(expectedSQL, sql);
			
		}

		@Override
		ChartSeriesFactory getChartSeriesFactory() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}

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


import static org.junit.Assert.*;

import org.junit.Test;

import io.konig.dao.core.FieldPath;
import io.konig.dao.core.ShapeQuery;

public class BigQueryChartSeriesFactoryTest {

	@Test
	public void test() {
		
		
		TestableBigQueryChartSeriesFactory factory = new TestableBigQueryChartSeriesFactory();
		
		ShapeQuery query = ShapeQuery.newBuilder().build();
		EntityStructure struct = new EntityStructure("LoginActivity");
		
		FieldInfo intervalStart = new FieldInfo("intervalStart");
		intervalStart.setStereotype(Stereotype.DIMENSION);
		
		FieldInfo count = new FieldInfo("count");
		count.setStereotype(Stereotype.MEASURE);
		
		FieldPath dimension = new FieldPath("timeInterval.intervalStart");
		FieldPath measure = new FieldPath("count");
		
		String actual = factory.sql(query, struct, dimension, measure);
		String expected = "SELECT timeInterval.intervalStart, count FROM LoginActivity";
		assertEquals(expected, actual);
	}

}

package io.konig.data.app.common;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.dao.core.ConstraintOperator;
import io.konig.dao.core.Format;
import io.konig.dao.core.FusionCharts;
import io.konig.dao.core.PredicateConstraint;
import io.konig.dao.core.DataFilter;
import io.konig.dao.core.ShapeQuery;
import io.konig.dao.core.ShapeReadService;

public class ExtentTest {
	
	@Mock
	private ShapeReadService shapeReadService;
	
	@Captor
	private ArgumentCaptor<ShapeQuery> queryCaptor;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test() throws Exception {
		
		
		URI defaultShapeId = uri("http://example.com/shapes/Person");
		
		Extent e = new Extent("/person", defaultShapeId, shapeReadService);
		StringWriter out = new StringWriter();
		
		URI individualId = uri("http://example.com/person/1234");
		e.writeNamedIndividual(individualId, out, Format.JSONLD);

		verify(shapeReadService).execute(queryCaptor.capture(), eq(out), eq(Format.JSONLD));
		
		ShapeQuery query = queryCaptor.getValue();
		
		assertEquals(defaultShapeId.stringValue(), query.getShapeId());
		
		DataFilter filter = query.getFilter();
		assertEquals(PredicateConstraint.class, filter.getClass());
		
		PredicateConstraint p = (PredicateConstraint) filter;
		assertEquals(ConstraintOperator.EQUAL,  p.getOperator());
		assertEquals("id", p.getPropertyName());
		assertEquals(individualId.stringValue(), p.getValue());
	}
	
	@Test
	public void testQueryParam() throws DataAppException {
		ExtentContainer cont = new ExtentContainer();
		assertTrue(cont.validateQueryParam("timeInterval.minInclusive", "2015-06-01", XMLSchema.DATE));
		assertTrue(cont.validateQueryParam(".view", FusionCharts.BAR_MEDIA_TYPE, XMLSchema.STRING));
		assertTrue(cont.validateQueryParam(".aggregate", "avg" , XMLSchema.STRING));
		assertTrue(cont.validateQueryParam(".xSort", "desc" , XMLSchema.STRING));
		assertTrue(cont.validateQueryParam(".ySort", "asc" , XMLSchema.STRING));
		assertTrue(cont.validateQueryParam(".limit", "100" , XMLSchema.LONG));
		assertTrue(cont.validateQueryParam(".offset", "100" , XMLSchema.LONG));		
		
		//Negative Test Cases
			
		try {
			cont.validateQueryParam("timeInterval.minInclusive", "2015-06-01SDS" , XMLSchema.DATE);
		}catch(Exception ex) {
			assertEquals("IllegalArgumentException : Invalid format key : timeInterval.minInclusive  /  value : 2015-06-01SDS", ex.getMessage());
		}
		
		try {
			cont.validateQueryParam(".aggregate", "avgs" , XMLSchema.STRING);
		}catch(Exception ex) {
			assertEquals("IllegalArgumentException : Invalid format key : .aggregate  /  value : avgs", ex.getMessage());
		}
		
		try {
			cont.validateQueryParam(".xSort", "desc@@@" , XMLSchema.STRING);
		}catch(Exception ex) {
			assertEquals("IllegalArgumentException : Invalid format key : .xSort  /  value : desc@@@", ex.getMessage());
		}
		
		try {
			cont.validateQueryParam(".limit", "1000s" , XMLSchema.STRING);
		}catch(Exception ex) {
			assertEquals("IllegalArgumentException : Invalid format key : .limit  /  value : 1000s", ex.getMessage());
		}
		
		try {
			cont.validateQueryParam(".view", "vnd.pearson.chart.fusioncharts.bar<script>" , XMLSchema.STRING);
		}catch(Exception ex) {
			assertEquals("IllegalArgumentException : Invalid format key : .view  /  value : vnd.pearson.chart.fusioncharts.bar<script>", ex.getMessage());
		}
		
		assertEquals("Select * from table where name = \\''", cont.escapeUtils("Select * from table where name = '"));
		assertEquals("ASAS&lt;script&gt;alert()&lt;\\/script&gt;", cont.escapeUtils("ASAS<script>alert()</script>"));
		assertEquals("grade.name" , cont.escapeUtils("grade.name"));
		assertEquals("uniqueCount", cont.escapeUtils("uniqueCount"));
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

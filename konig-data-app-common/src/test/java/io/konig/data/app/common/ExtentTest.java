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

import io.konig.dao.core.ConstraintOperator;
import io.konig.dao.core.Format;
import io.konig.dao.core.PredicateConstraint;
import io.konig.dao.core.ShapeFilter;
import io.konig.dao.core.ShapeQuery;
import io.konig.dao.core.ShapeReadService;
import io.konig.yaml.Yaml;

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
		
		ShapeFilter filter = query.getFilter();
		assertEquals(PredicateConstraint.class, filter.getClass());
		
		PredicateConstraint p = (PredicateConstraint) filter;
		assertEquals(ConstraintOperator.EQUAL,  p.getOperator());
		assertEquals("id", p.getPropertyName());
		assertEquals(individualId.stringValue(), p.getValue());
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

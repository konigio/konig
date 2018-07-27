package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.shacl.PropertyConstraint;

public class BigQueryDatatypeMapperTest {

	BigQueryDatatypeMapper map = new BigQueryDatatypeMapper();
	PropertyConstraint constraint = new PropertyConstraint();
	
	@Test
	public void test() {

		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.BYTE));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.INT));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.INTEGER));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.LONG));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.NEGATIVE_INTEGER));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.NON_NEGATIVE_INTEGER));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.NON_POSITIVE_INTEGER));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.SHORT));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.UNSIGNED_BYTE));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.UNSIGNED_INT));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.UNSIGNED_LONG));
		assertEquals(BigQueryDatatype.INT64, type(XMLSchema.UNSIGNED_SHORT));
		
		assertEquals(BigQueryDatatype.FLOAT64, type(XMLSchema.FLOAT));
		assertEquals(BigQueryDatatype.FLOAT64, type(XMLSchema.DECIMAL));
		assertEquals(BigQueryDatatype.FLOAT64, type(XMLSchema.DOUBLE));
		assertEquals(BigQueryDatatype.BOOLEAN, type(XMLSchema.BOOLEAN));
		
	}

	private BigQueryDatatype type(URI datatype) {
		constraint.setDatatype(datatype);
		return map.type(constraint);
	}

}

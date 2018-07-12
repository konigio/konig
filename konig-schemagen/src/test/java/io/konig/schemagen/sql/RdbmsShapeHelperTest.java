package io.konig.schemagen.sql;

import static org.junit.Assert.assertTrue;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.shacl.Shape;

public class RdbmsShapeHelperTest extends AbstractRdbmsShapeGeneratorTest {
	@Test
	public void testGetShMaxCardinality() throws Exception {
		load("src/test/resources/rdbms-shape-helper");
		Integer shMaxCardinality=rdbmsShapeHelper.getShMaxCardinality(uri("http://schema.org/address"));
		assertTrue(shMaxCardinality==3);
	}
	@Test
	public void testGetOwlMaxCardinality() throws Exception{
		load("src/test/resources/rdbms-shape-helper");
		Integer owlMaxCardinality=rdbmsShapeHelper.getOwlMaxCardinality(uri("http://schema.org/givenName"));
		assertTrue(owlMaxCardinality==1);
		owlMaxCardinality=rdbmsShapeHelper.getOwlMaxCardinality(uri("http://schema.org/address"));
		assertTrue(owlMaxCardinality==5);
		owlMaxCardinality=rdbmsShapeHelper.getOwlMaxCardinality(uri("http://schema.org/owns"));
		assertTrue(owlMaxCardinality==3);
		
	}
	
	private URI uri(String text) {
		return new URIImpl(text);
	}
}

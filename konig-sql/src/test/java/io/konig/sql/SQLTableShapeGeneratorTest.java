package io.konig.sql;

/*
 * #%L
 * Konig SQL
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
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.vocab.Schema;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SQLTableShapeGeneratorTest {

	@Test
	public void test() {
		
		String text =
			"CREATE TABLE registrar.Person ("
			+ "givenName VARCHAR(64), "
			+ "familyName VARCHAR(64), "
			+ "taxID VARCHAR(64) PRIMARY KEY"
			+ ");";
		
		SQLParser parser = new SQLParser();
		
		SQLTableSchema table  = parser.parseTable(text);
		
		SQLTableNamerImpl tableNamer = new SQLTableNamerImpl("http://example.com/sql/");
		SQLTableShapeGenerator generator = new SQLTableShapeGenerator(tableNamer);
		
		Shape shape = generator.toShape(table);
		
		
		assertEquals(uri("http://example.com/sql/registrar/PersonShape"), shape.getId());
		
		PropertyConstraint pc = shape.getPropertyConstraint(uri("http://example.com/sql/registrar/PersonShape#taxID"));
		assertTrue(pc != null);
		assertEquals(pc.getDatatype(), XMLSchema.STRING);
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

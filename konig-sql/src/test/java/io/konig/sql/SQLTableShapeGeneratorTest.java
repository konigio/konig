package io.konig.sql;

import static org.junit.Assert.*;

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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Path;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class SQLTableShapeGeneratorTest {
	
	private SQLSchemaManager schemaManager = new SQLSchemaManager();
	SQLParser parser = new SQLParser(schemaManager);
	
	SQLTableShapeGenerator generator = new SQLTableShapeGenerator();
	
	@Test
	public void testRegistrar() throws Exception {
		
		parseFile("SQLTableShapeGeneratorTest/registrar.sql");
		
		SQLSchema schema = schemaManager.getSchemaByName("registrar");
		
		SQLTableSchema personTable = schema.getTableByName("Person");
		
		Shape personShape = generator.toShape(personTable);
		
		assertTrue(personShape != null);
		
		SQLTableSchema courseInstanceTable = schema.getTableByName("CourseSection");
		Shape courseInstanceShape = generator.toShape(courseInstanceTable);
		
		assertTrue(courseInstanceShape != null);
		
		SQLTableSchema membershipTable = schema.getTableByName("CourseSectionPerson");
		Shape membershipShape = generator.toShape(membershipTable);
		
		assertTrue(membershipShape != null);
		
		SQLTableSchema roleTable = schema.getTableByName("Role");
		Shape roleShape = generator.toShape(roleTable);
		
		assertTrue(roleShape != null);
		
		PropertyConstraint p = roleShape.getPropertyConstraint(uri("http://example.com/ns/alias/role_id"));
		assertTrue(p != null);
		
		Path path = p.getCompiledEquivalentPath();
		assertTrue(path != null);
		assertEquals("/registrarId", path.toString());
		
		
	}

	private Reader getReader(String resource) {
		return new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resource));
	}
	
	private void parseFile(String resource) throws RDFParseException, RDFHandlerException, IOException {
		Reader reader = getReader(resource);
		parser.parseAll(reader);
	}
	
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

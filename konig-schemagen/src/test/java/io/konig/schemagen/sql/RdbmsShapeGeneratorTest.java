package io.konig.schemagen.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.schemagen.SchemaGeneratorTest;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.io.ShapeWriter;
import io.konig.spreadsheet.WorkbookLoader;

public class RdbmsShapeGeneratorTest extends SchemaGeneratorTest {
	
	private RdbmsShapeGenerator shapeGenerator = new RdbmsShapeGenerator("(.*)Shape$","$1RdbmsShape");
	
	@Test
	public void testSnakeCase() throws Exception {
		
		load("src/test/resources/rdbms-shape-generator");
		
		
		URI shapeId = iri("http://example.com/shapes/TargetPersonShape");
		
		
		Shape logicalShape = shapeManager.getShapeById(shapeId);
		Shape rdbmsShape = shapeGenerator.createRdbmsShape(logicalShape);
		String changeCase = shapeGenerator.changeToSnakeCase("GivenName");
		String snakeCase = shapeGenerator.changeToSnakeCase("FAMILY_NAME");
		assertTrue(rdbmsShape != null);
		assertEquals("GIVEN_NAME",changeCase);
		assertNull(snakeCase);
		
	}
	


}

package io.konig.schemagen.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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


import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import io.konig.maven.FileUtil;
import io.konig.schemagen.SchemaGeneratorTest;
import io.konig.shacl.Shape;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeWriter;

public class RdbmsShapeHandlerTest extends SchemaGeneratorTest {
	
	private RdbmsShapeGenerator shapeGenerator = new RdbmsShapeGenerator("(.*)Shape$","$1RdbmsShape","http://example.com/ns/alias/");
	private RdbmsShapeHandler handler = null;
	private File targetFolder = new File("target/test/rdbms-shape-generator");
	
	@Before
	public void setUp() throws IOException {

		if (targetFolder.exists()) {
			FileUtils.forceDelete(targetFolder);
		}
		File sourceFolder = new File("src/test/resources/rdbms-shape-generator");
		FileUtil.copyDirectory(sourceFolder, targetFolder);
		
		File shapesDir = new File(targetFolder, "shapes");
				
		ShapeFileGetter fileGetter = new ShapeFileGetter(shapesDir, nsManager);
		ShapeWriter shapeWriter = new ShapeWriter();
		
		handler = new RdbmsShapeHandler(shapeGenerator, fileGetter, shapeWriter, nsManager);
	}
	
	@Test
	public void testVisit() throws Exception {
		
		load("src/test/resources/rdbms-shape-generator");
		
		
		URI shapeId = iri("http://example.com/shapes/TargetPersonShape");
		
		
		Shape logicalShape = shapeManager.getShapeById(shapeId);
		
		handler.visit(logicalShape);

		assertTrue(logicalShape!= null);
		assertNotNull(handler);
	}

}

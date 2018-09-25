package io.konig.schemagen.sql;

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.maven.FileUtil;
import io.konig.shacl.Shape;
import io.konig.shacl.impl.ShapeInjector;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeWriter;

public class RdbmsShapeHandlerTest extends AbstractRdbmsShapeGeneratorTest {
	
	private RdbmsShapeHandler handler = null;
	private File targetFolder = new File("target/test/rdbms-shape-generator");
	
	@Before
	public void setUp() throws IOException {

		if (targetFolder.exists()) {
			FileUtils.forceDelete(targetFolder);
		}
		File sourceFolder = new File("src/test/resources/rdbms-shape-generator");
		FileUtil.copyDirectory(sourceFolder, targetFolder);
		
		
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
	

	protected void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		super.load(path);
		
		ShapeInjector callback = new ShapeInjector(shapeManager);
		handler = new RdbmsShapeHandler(callback, shapeGenerator);
	}

}

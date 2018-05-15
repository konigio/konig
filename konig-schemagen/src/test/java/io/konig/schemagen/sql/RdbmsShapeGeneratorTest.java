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
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeLoader;
import io.konig.shacl.io.ShapeWriter;
import io.konig.spreadsheet.WorkbookLoader;

public class RdbmsShapeGeneratorTest {
	
	@Test
	public void testValidateLocalNames() throws Exception {
		AwsShapeConfig.init();
		RdbmsShapeGenerator shapeGenerator = new RdbmsShapeGenerator("(.*)Shape$","$1RdbmsShape");
		InputStream input = getClass().getClassLoader().getResourceAsStream("rdbms/rdbmsshapegenerator.xlsx");
		Workbook book = WorkbookFactory.create(input);
		Graph graph = new MemoryGraph();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		graph.setNamespaceManager(nsManager);
		
		WorkbookLoader loader = new WorkbookLoader(nsManager);
		loader.load(book, graph);
		input.close();
		
		URI shapeId = uri("http://example.com/shapes/TargetPersonShape");
		
		ShapeManager s = new MemoryShapeManager();
		
		ShapeLoader shapeLoader = new ShapeLoader(s);
		shapeLoader.load(graph);
		
		
		Shape shape = s.getShapeById(shapeId);
		shapeGenerator.validateLocalNames(shape);
		
		shapeGenerator.createRdbmsShape(shape);

				ShapeFileGetter fileGetter = new ShapeFileGetter(new File(""), nsManager);
    	RdbmsShapeGenerator generator = new RdbmsShapeGenerator("(.*)Shape$","$1RdbmsShape");
    	ShapeWriter shapeWriter = new ShapeWriter();
    	 RdbmsShapeHandler handler = new RdbmsShapeHandler(generator, fileGetter, shapeWriter, nsManager);
    	 handler.visit(shape);

		assertTrue(shape!=null);
	
		
	}
	
	private URI uri(String text) {
		return new URIImpl(text);
	}


}

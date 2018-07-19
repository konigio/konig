package io.konig.schemagen;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.schemagen.sql.SqlDatatypeMapper;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMaxRowLength;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeLoader;

public class CalculateMaxRowSizeTest {
	private ShapeManager shapeManager = new MemoryShapeManager();
	private ShapeLoader shapeLoader = new ShapeLoader(shapeManager);
	private SqlDatatypeMapper datatypeMapper = new SqlDatatypeMapper();

	CalculateMaximumRowSize calculateMaxRowSize = new CalculateMaximumRowSize();

	@Test
	public void testReadShapeFile()  throws Exception {
		
		AwsShapeConfig.init();
	 		 NamespaceManager nsManager = new MemoryNamespaceManager();
		
			MemoryGraph graph = new MemoryGraph();
			
			RdfUtil.loadTurtle(new File("src/test/resources/calMaxRowSize"), graph, nsManager);
			
			shapeLoader.load(graph);
			
			
		URI shapeId = uri("http://example.com/shapes/SourcePersonRdbmsShape");
			Shape shape = shapeManager.getShapeById(shapeId);
	
			File outDir = new File("src/test/resources/calMaxRowSize");
			ShapeFileGetter fileGetter = new ShapeFileGetter(outDir, nsManager);
			
			calculateMaxRowSize.addMaximumRowSize(shape,fileGetter,nsManager);
			assertTrue(shape!=null);
			List<ShapeMaxRowLength> shapemaxList = new ArrayList<ShapeMaxRowLength>();
			shapemaxList= shape.getShapeMaxRowLengthList();
			assertEquals(shapemaxList.size(),1);
			for(ShapeMaxRowLength shapeRow: shapemaxList){
				assertEquals(shapeRow.getMaxRowLength(),3012);
				assertEquals(shapeRow.getTargetDatasource().toString(),"http://www.konig.io/ns/aws/host/devHost/databases/schema1/tables/SourcePersonRdbmsShape");
			}

				}
						
private URI uri(String text) {
	return new URIImpl(text);
}

}

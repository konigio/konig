package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class BasicTransformServiceTest {
	
	private MemoryGraph graph = new MemoryGraph(MemoryNamespaceManager.getDefaultInstance());
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private ShowlService service = new ShowlServiceImpl(reasoner);
	private ShowlNodeShapeBuilder nodeShapeBuilder = new ShowlNodeShapeBuilder(service, service);
	private ReceivesDataFromSourceNodeFactory sourceNodeFactory = new ReceivesDataFromSourceNodeFactory(nodeShapeBuilder, graph);
	private ShapeManager shapeManager = new MemoryShapeManager();
	
	private BasicTransformService transformService = new BasicTransformService(service, service, sourceNodeFactory);

	@Test
	public void testTabularMapping() throws Exception {
		
		load("src/test/resources/BasicTransformServiceTest/tabular-mapping");
		URI shapeId = uri("http://example.com/ns/shape/PersonTargetShape");
		Shape targetShape = shapeManager.getShapeById(shapeId);
		assertTrue(targetShape != null);
		
		ShowlNodeShape targetNode = nodeShapeBuilder.buildNodeShape(null, targetShape);
		setDataSource(targetNode, Konig.GoogleBigQueryTable);
		
		Set<ShowlPropertyShapeGroup> group = transformService.computeTransform(targetNode);
		assertTrue(group.isEmpty());
		
		ShowlDirectPropertyShape givenName = targetNode.getProperty(Schema.givenName);
		ShowlExpression e = givenName.getSelectedExpression();
		assertTrue(e instanceof ShowlDirectPropertyExpression);
		
		ShowlDirectPropertyExpression direct = (ShowlDirectPropertyExpression) e;
		assertEquals("first_name", direct.getSourceProperty().getPredicate().getLocalName());
		
		ShowlPropertyShape id = targetNode.findOut(Konig.id);
		assertTrue(id != null);
		
		ShowlExpression idExpression = id.getSelectedExpression();
		assertTrue(idExpression instanceof ShowlFunctionExpression);
		
		
		
	}

	private void setDataSource(ShowlNodeShape targetNode, URI sourceType) {
		for (DataSource ds : targetNode.getShape().getShapeDataSource()) {
			if (ds.isA(sourceType)) {
				targetNode.setShapeDataSource(new ShowlDataSource(targetNode, ds));
			}
		}
		
	}

	private URI uri(String stringValue) {
		return new URIImpl(stringValue);
	}

	private void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		File sourceDir = new File(path);
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		
		ShowlClassProcessor classProcessor = new ShowlClassProcessor(service, service);
		classProcessor.buildAll(shapeManager);
		
	}

}

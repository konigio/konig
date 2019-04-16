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
import java.util.List;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class MappingStrategyTest {

	private ShowlManager showlManager;
	private NamespaceManager nsManager;
	private ObsoleteMappingStrategy strategy = new ObsoleteMappingStrategy();

	@Test
	public void testTabular() throws Exception {
		load("src/test/resources/MappingStrategyTest/tabular");
		URI shapeId = uri("http://example.com/ns/shape/PersonTargetShape");
		ShowlNodeShape node = showlManager.getNodeShapeSet(shapeId).findAny();
		
		List<ShowlDirectPropertyShape> remains = strategy.selectMappings(showlManager, node);
		
		ShowlPropertyShape givenName = node.findProperty(Schema.givenName);
		
		ShowlMapping mapping = givenName.getSelectedMapping();
		
		assertTrue(mapping != null);
		assertEquals("first_name", mapping.findOther(givenName).getPredicate().getLocalName());
		
		ShowlPropertyShape id = node.findProperty(Konig.id);
		mapping = id.getSelectedMapping();
		assertTrue(mapping != null);
		
		assertTrue(remains.isEmpty());
		
		assertEquals(1, node.getSelectedJoins().size());
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void load(String filePath) throws RDFParseException, RDFHandlerException, IOException {
		File sourceDir = new File(filePath);
		nsManager = new MemoryNamespaceManager();
		Graph graph = new MemoryGraph(nsManager);
		OwlReasoner reasoner = new OwlReasoner(graph);
		ShapeManager shapeManager = new MemoryShapeManager();
		
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		
		showlManager = new ShowlManager(shapeManager, reasoner);
		showlManager.load();
		
	}

}

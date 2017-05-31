package io.konig.core.impl;

/*
 * #%L
 * Konig Core
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
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.Dbpedia;

public class URIVertexTest {

	@Test
	public void testParts() {
		Graph graph = new MemoryGraph();
		URI id = new URIImpl(Dbpedia.CATEGORY_BASE_URI + "Nanotechnology");
		
		Vertex v = graph.vertex(id);
		Resource vid = v.getId();
		assertTrue(vid instanceof URIVertex);
		
		URI uri = (URI) vid;
		
		assertEquals(Dbpedia.CATEGORY_BASE_URI, uri.getNamespace());
		assertEquals("Nanotechnology", uri.getLocalName());
		
	}

}

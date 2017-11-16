package io.konig.gcp.datasource;

/*
 * #%L
 * Konig Google Cloud Platform Model
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

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.google.api.services.bigquery.model.ExternalDataConfiguration;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.GCP;

public class GoogleBigQueryTableTest {

	@Test
	public void testDeserializer() {
		
		Graph graph = new MemoryGraph();
		URI sourceId = uri("gs://foobar");

		Vertex v = graph.vertex();
		
		graph.builder()
			.beginSubject(v)
				.addProperty(GCP.sourceUris, sourceId)
			.endSubject();
		
		SimplePojoFactory factory = new SimplePojoFactory();
		ExternalDataConfiguration external = factory.create(v, ExternalDataConfiguration.class);
		
		List<String> list = external.getSourceUris();
		
		assertTrue(list!=null);
		
		assertEquals(1, list.size());
		assertEquals(sourceId.stringValue(), list.get(0));
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

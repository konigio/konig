package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
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


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;

public class DataCatalogPathFactoryTest {
	
	private DataCatalogPathFactory pathFactory;
	
	@Before
	public void setUp() {

		Graph graph = new MemoryGraph();
		OwlReasoner owl = new OwlReasoner(graph);
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add(DataCatalogBuilder.DCAT_PREFIX, DataCatalogBuilder.CATALOG_BASE_URI);
		
		pathFactory = new DataCatalogPathFactory(owl, nsManager, DataCatalogBuilder.DCAT_PREFIX);
	}

	@Test
	public void testDatasourceSummaryToJsonDdl() throws Exception {
		
		URI sqlFile = uri("urn:datacatalog/ddl/GoogleBigQueryTable/schema.Person.json");
		URI datasourceSummary = DataCatalogBuilder.DATASOURCE_SUMMARY_URI;
		
		String path = pathFactory.relativePath(datasourceSummary, sqlFile);
		
		assertEquals("ddl/GoogleBigQueryTable/schema.Person.json", path);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

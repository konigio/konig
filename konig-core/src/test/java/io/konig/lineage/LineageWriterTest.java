package io.konig.lineage;

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

import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.datasource.DataSourceManager;

public class LineageWriterTest {
	
	LineageWriter lineageWriter = new LineageWriter();
	LineageLoader lineageLoader = new LineageLoader();

	@Test
	public void test() throws Exception {
		
		DataSource source = new DataSource();
		source.addType(Konig.DataSource);
		URI sourceId = uri("http://example/ds/source");
		
		source.setId(sourceId);
		
		DatasourceProperty first_name = new DatasourceProperty();
		first_name.setId(uri("http://example.com/ds/source/property/first_name"));
		first_name.setPropertyPath(path(uri("http://example.com/ns/alias/first_name")));
		
		DatasourceProperty last_name = new DatasourceProperty();
		last_name.setId(uri("http://example.com/ds/source/property/last_name"));
		last_name.setPropertyPath(path(uri("http://example.com/ns/alias/last_name")));
		
		
		source.addDatasourceProperty(first_name);
		source.addDatasourceProperty(last_name);
		
		DataSource target = new DataSource();
		target.setId(uri("http://example.com/ds/target"));
		
		DatasourceProperty name = new DatasourceProperty();
		name.setId(uri("http://example.com/ds/target/property/name"));
		name.setPropertyPath(path(Schema.name));
		
		target.addDatasourceProperty(name);
		
		PropertyGenerator generator = new PropertyGenerator();
		generator.setId(uri("http://example.com/ds/target/property/name/generator"));
		
		name.setGeneratedFrom(generator);
		
		generator.setGeneratorOutput(name);
		
		generator.addGeneratorInput(first_name);
		generator.addGeneratorInput(last_name);
		
		first_name.addGeneratorInputOf(generator);
		last_name.addGeneratorInputOf(generator);
		
		
		
		
		StringWriter out = new StringWriter();
		
		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		nsManager.add("alias", "http://example.com/ns/alias/");
		
		
		lineageWriter.writeDatasourceProperties(out, nsManager);
		
		String turtleText = out.toString();
		System.out.println(turtleText);
		
		DataSourceManager manager = DataSourceManager.getInstance();
		manager.clear();
		
		Graph graph = new MemoryGraph();
		RdfUtil.loadTurtle(graph, new StringReader(turtleText), "");
		
		lineageLoader.load(graph);
		
		DataSource targetLoaded = manager.findDataSourceById(target.getId());
		assertTrue(targetLoaded != null);
		
		DatasourceProperty nameLoaded = targetLoaded.findPropertyByPredicate(Schema.name);
		assertTrue(nameLoaded != null);
		
		PropertyGenerator generatorLoaded = nameLoaded.getGeneratedFrom();
		assertEquals(2, generatorLoaded.getGeneratorInput().size());
		
		
		
	}
	
	private DatasourcePropertyPath path(URI...p) {
		DatasourcePropertyPath list = new DatasourcePropertyPath();
		for (URI predicate : p) {
			list.add(predicate);
		}
		return list;
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

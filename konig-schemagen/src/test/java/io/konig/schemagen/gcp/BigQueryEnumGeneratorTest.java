package io.konig.schemagen.gcp;

/*
 * #%L
 * Konig Schema Generator
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


import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class BigQueryEnumGeneratorTest {
	

	@Test
	public void test() throws Exception {
		
		Graph graph = new MemoryGraph();
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.Male, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Male, Schema.name, literal("Male"));
		ShapeManager shapeManager = new MemoryShapeManager();
		File outDir = new File("target/test/BigQueryEnumGeneratorTest");
		DatasetMapper datasetMapper = new SimpleDatasetMapper("example");
		BigQueryTableMapper tableMapper = new LocalNameTableMapper();
		DataFileMapper dataFileMapper = new DataFileMapperImpl(outDir, datasetMapper, tableMapper);
		BigQueryEnumGenerator generator = new BigQueryEnumGenerator(shapeManager);
		
		generator.generate(graph, dataFileMapper);
		
	}

	private Value literal(String value) {
		return new LiteralImpl(value);
	}

}

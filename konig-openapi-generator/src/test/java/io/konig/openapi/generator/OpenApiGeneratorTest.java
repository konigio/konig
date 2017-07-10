package io.konig.openapi.generator;

/*
 * #%L
 * Konig OpenAPI Generator
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


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;

import org.junit.Test;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.jsonschema.generator.JsonSchemaGenerator;
import io.konig.jsonschema.generator.JsonSchemaNamer;
import io.konig.jsonschema.generator.JsonSchemaTypeMapper;
import io.konig.jsonschema.generator.SimpleJsonSchemaNamer;
import io.konig.jsonschema.generator.SimpleJsonSchemaTypeMapper;
import io.konig.openapi.model.OpenAPI;
import io.konig.openapi.model.PathMap;
import io.konig.shacl.ShapeFilter;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.yaml.AnchorFeature;
import io.konig.yaml.Yaml;
import io.konig.yaml.YamlWriterConfig;

public class OpenApiGeneratorTest {
	
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private JsonSchemaNamer namer = new ShapeLocalNameJsonSchemaNamer();
	private JsonSchemaTypeMapper typeMapper = new SimpleJsonSchemaTypeMapper();
	private Graph graph = new MemoryGraph(nsManager);
	private ShapeFilter shapeFilter = new RootClassShapeFilter(graph);
	private JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(nsManager, null, typeMapper);
	private OpenApiGenerator generator = new OpenApiGenerator(namer, schemaGenerator, shapeFilter);
	private ShapeManager shapeManager = new MemoryShapeManager();

	@Test
	public void test() throws Exception {
		
		try (FileReader infoReader = new FileReader("src/test/resources/sample-api-info.yaml")) {

			load("src/test/resources/openapi-sample");
			
			StringWriter output = new StringWriter();
			OpenApiGenerateRequest request = new OpenApiGenerateRequest()
				.setShapeManager(shapeManager)
				.setOpenApiInfo(infoReader)
				.setWriter(output);
			
			generator.generate(request);
	
			String text = output.toString();
			System.out.println(text);
		}
		
	}

	private void load(String path) throws Exception {
		File file = new File(path);
		RdfUtil.loadTurtle(file, graph, shapeManager);
	}

}

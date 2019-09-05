package io.konig.schemagen.jsonschema.doc;

/*
 * #%L
 * Konig Schema Generator
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


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.schemagen.jsonschema.JsonSchemaGenerator;
import io.konig.schemagen.jsonschema.JsonSchemaNamer;
import io.konig.schemagen.jsonschema.JsonSchemaTypeMapper;
import io.konig.schemagen.jsonschema.impl.SimpleJsonSchemaNamer;
import io.konig.schemagen.jsonschema.impl.SimpleJsonSchemaTypeMapper;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeMediaTypeNamer;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;

public class JsonSchemaDocumentationGeneratorTest {

	private ShapeMediaTypeNamer mediaTypeNamer = new SimpleShapeMediaTypeNamer();
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private MemoryGraph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();

	private JsonSchemaNamer namer = new SimpleJsonSchemaNamer("/json-schema", mediaTypeNamer);
	private JsonSchemaTypeMapper typeMapper = new SimpleJsonSchemaTypeMapper();
	private JsonSchemaGenerator schemaGenerator = new JsonSchemaGenerator(namer, nsManager, typeMapper);
	private JsonSchemaDocumentationGenerator generator  = new JsonSchemaDocumentationGenerator();

	@Test
	public void test() throws Exception {
		String expected = "{\n" + 
				"   \"@context\": { -- Encapsulates contextual information including the default language for text strings in this record.\n" + 
				"      \"@language\": string  -- The BCP-47 language code for the default language of text strings in the enclosing resource.\n" + 
				"   },\n" + 
				"   \"jobTitle\": string  -- The person's job title\n" + 
				"}\n" + 
				"";
		URI shapeId = uri("http://example.com/ns/shape/PersonShape");
		String text = generate("src/test/resources/JsonSchemaGeneratorTest/jsonld", shapeId);
		assertEquals(text, expected);
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

	private String generate(String path, URI shapeId) throws RDFParseException, RDFHandlerException, IOException {
		load(path);
		Shape shape = shapeManager.getShapeById(shapeId);
		assertTrue(shape != null);
		ObjectNode node = schemaGenerator.generateJsonSchema(shape);
		
		StringWriter out = new StringWriter();
		
		generator.write(out, node);
		
		return out.toString();
	}

	private void load(String path) throws RDFParseException, RDFHandlerException, IOException {
		
		File file = new File(path);
		RdfUtil.loadTurtle(file, graph, shapeManager);
	}

}

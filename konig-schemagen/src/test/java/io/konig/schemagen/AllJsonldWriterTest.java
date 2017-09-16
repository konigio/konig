package io.konig.schemagen;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.konig.core.Graph;
import io.konig.core.GraphBuilder;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.VANN;

public class AllJsonldWriterTest {

	@Test
	public void test() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		nsManager.add("owl", "http://www.w3.org/2002/07/owl#");
		nsManager.add("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		nsManager.add("vann", "http://purl.org/vocab/vann/");
		nsManager.add("sh", "http://www.w3.org/ns/shacl#");
		
		
		StringWriter out = new StringWriter();
		Graph graph = new MemoryGraph();
		
		GraphBuilder builder = new GraphBuilder(graph);
		builder.beginSubject(Schema.NAMESPACE_URI)
			.addProperty(RDF.TYPE, OWL.ONTOLOGY)
			.addLiteral(VANN.preferredNamespacePrefix, "schema")
		;
		builder.beginSubject(Schema.Person)
			.addProperty(RDF.TYPE, OWL.CLASS)
			.addLiteral(RDFS.COMMENT, "A person (alive, dead, undead, or fictional)")
		;
		builder.beginSubject(uri("http://example.com/shapes/v1/schema/Person"))
			.addProperty(RDF.TYPE, SH.Shape)
			.beginBNode(SH.property)
				.addProperty(SH.path, Schema.givenName)
			.endSubject()
			.beginBNode(SH.property)
				.addProperty(SH.path, Schema.familyName)
			.endSubject()
		;
		
		JsonFactory factory = new JsonFactory();
		JsonGenerator json = factory.createGenerator(out);
		json.useDefaultPrettyPrinter();
		
		AllJsonldWriter allWriter = new AllJsonldWriter();
		
		allWriter.writeJSON(nsManager, graph, null, json);
		
		String text = out.toString();
		System.out.println(text);
		
		ObjectMapper mapper = new ObjectMapper();
		
		ObjectNode node = (ObjectNode) mapper.readTree(text);
		
		ObjectNode context = (ObjectNode) node.get("@context");
		assertTrue(context != null);
		
		assertEquals("http://schema.org/", context.get("schema").asText());
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

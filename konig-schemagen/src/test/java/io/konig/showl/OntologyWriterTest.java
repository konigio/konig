package io.konig.showl;

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


import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.VANN;

public class OntologyWriterTest {

	@Test
	public void test() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		nsManager.add("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		nsManager.add("owl", "http://www.w3.org/2002/07/owl#");
		nsManager.add("vann", "http://purl.org/vocab/vann/");
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject(Schema.NAMESPACE_URI)
				.addProperty(RDF.TYPE, OWL.ONTOLOGY)
				.addLiteral(VANN.preferredNamespacePrefix, "schema")
			.endSubject()
			.beginSubject(Schema.Person)
				.addProperty(RDF.TYPE, OWL.CLASS)
				.addLiteral(RDFS.LABEL, "Person")
			.endSubject()
			;
		
		graph.setNamespaceManager(nsManager);
		
		File baseDir = new File("target/test/OntologyWriter").getAbsoluteFile();
		baseDir.mkdirs();
		
		OntologyFileGetter namer = new OntologyFileGetter(baseDir, nsManager);
		
		OntologyWriter writer = new OntologyWriter(namer);
		writer.writeOntologies(graph);
		
		
		String expected = "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +  
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" + 
				"@prefix schema: <http://schema.org/> .\n" + 
				"@prefix vann: <http://purl.org/vocab/vann/> .\n" + 
				"\n" + 
				"schema: a owl:Ontology ; \n" + 
				"	vann:preferredNamespacePrefix \"schema\" . \n" + 
				"\n" + 
				"schema:Person a owl:Class ; \n" + 
				"	rdfs:label \"Person\" .";
		
		String actual = readFile("target/test/OntologyWriter/schema.ttl").replace("\r", "").trim();
		
		assertEquals(expected, actual);
	}
	
	private String readFile(String path) throws IOException {
		byte[] data = Files.readAllBytes(Paths.get(path));
		return new String(data, "UTF-8");
	}
}

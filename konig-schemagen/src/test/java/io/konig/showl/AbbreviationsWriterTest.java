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
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Konig;

public class AbbreviationsWriterTest {

	@Test
	public void test() throws Exception {
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("abbrev", "https://example.com/exampleModel/AbbreviationScheme/");
		nsManager.add("konig", "http://www.konig.io/ns/core/");
		nsManager.add("skos", "http://www.w3.org/2004/02/skos/core#");
		
		Graph graph = new MemoryGraph();
		graph.builder()
			.beginSubject("https://example.com/exampleModel/AbbreviationScheme/")
				.addProperty(RDF.TYPE, SKOS.CONCEPT_SCHEME)
			.endSubject()
			.beginSubject("https://example.com/exampleModel/AbbreviationScheme/MANUFACTURING")
				.addProperty(RDF.TYPE, SKOS.CONCEPT)
				.addLiteral(SKOS.PREF_LABEL,"MANUFACTURING")
				.addLiteral(Konig.abbreviationLabel, "MFG")
				.addProperty(SKOS.IN_SCHEME,uri("https://example.com/exampleModel/AbbreviationScheme/"))
			.endSubject()
			;
		
		graph.setNamespaceManager(nsManager);
		
		File baseDir = new File("target/test/AbbreviationsWriter").getAbsoluteFile();
		baseDir.mkdirs();
		
		AbbreviationsWriter writer = new AbbreviationsWriter(baseDir);
		writer.writeAbbreviations(graph);
		
		
		String expected="@prefix abbrev: <https://example.com/exampleModel/AbbreviationScheme/> .\n"
							+"@prefix konig: <http://www.konig.io/ns/core/> .\n"
							+"@prefix skos: <http://www.w3.org/2004/02/skos/core#> .\n\n"
							+"abbrev: a skos:ConceptScheme . \n\n"
							+"abbrev:MANUFACTURING a skos:Concept ; \n\t" 
							+"skos:prefLabel \"MANUFACTURING\" ; \n\t" 
							+"konig:abbreviationLabel \"MFG\" ; \n\t" 
							+"skos:inScheme abbrev: .";
		
		String actual = readFile("target/test/AbbreviationsWriter/abbrev.ttl").replace("\r", "").trim();
		
		assertEquals(expected, actual);
	}
	
	private String readFile(String path) throws IOException {
		byte[] data = Files.readAllBytes(Paths.get(path));
		return new String(data, "UTF-8");
	}
	private URI uri(String text) {
		ValueFactory vf = new ValueFactoryImpl();
		return vf.createURI(text);
	}
}

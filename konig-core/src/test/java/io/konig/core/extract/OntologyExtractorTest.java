package io.konig.core.extract;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Schema;

public class OntologyExtractorTest {

	@Test
	public void test() throws Exception {
		
		
		Graph source = new MemoryGraph();
		Graph target = new MemoryGraph();
		InputStream input = getClass().getClassLoader().getResourceAsStream("extract/ontologyExtractorTest.ttl");
		RdfUtil.loadTurtle(source, input, "");
		input.close();
		
		Vertex ontology = source.getVertex(uri("http://schema.org/"));
		
		OntologyExtractor extractor = new OntologyExtractor();
		extractor.extract(ontology, target);
		
		assertValue(ontology, RDF.TYPE, OWL.ONTOLOGY);
		assertValue(ontology, RDFS.LABEL, "Schema.org");
		
		Vertex skosConcept = target.getVertex(SKOS.CONCEPT);
		assertTrue(skosConcept == null);
		
		Vertex givenName = target.getVertex(Schema.givenName);
		assertTrue(givenName != null);
		
		Vertex male = target.getVertex(uri("http://schema.org/Male"));
		assertTrue(male != null);
	}
	
	private URI uri(String text) {
		return new URIImpl(text);
	}
	
	private void assertValue(Vertex subject, URI predicate, String object) {
		Literal value = new LiteralImpl(object);
		assertValue(subject, predicate, value);
	}


	private void assertValue(Vertex subject, URI predicate, Value object) {
		
		List<Value> list = subject.asTraversal().out(predicate).toValueList();
		
		for (Value value : list) {
			if (value.equals(object)) {
				return;
			}
		}
		
		String message = MessageFormat.format("Triple not found: <{0}> <{1}> \"{2}\"", subject.getId().stringValue(), predicate.getLocalName(), object.stringValue());
		
		fail(message);
		
	}

}

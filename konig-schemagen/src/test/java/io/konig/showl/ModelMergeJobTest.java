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


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Schema;

public class ModelMergeJobTest {
	private ValueFactory factory = new ValueFactoryImpl();

	@Ignore
	public void test() throws Exception {
		
		File oldDir = new File("src/test/resources/showl/v1");
		File newDir = new File("src/test/resources/showl/v2");
		File mergeDir = new File("target/showl/test");
		mergeDir.mkdirs();
		
		PrintWriter reportStream = new PrintWriter(System.out);
		ModelMergeJob job = new ModelMergeJob(oldDir, newDir, mergeDir, reportStream);
		job.setReportEnabled(false);
		
		job.run();
		
		File targetFile = new File(mergeDir, "schema.ttl");
		assertTrue(targetFile.exists());
		
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		Graph target = new MemoryGraph();
		RdfUtil.loadTurtle(mergeDir, target, nsManager);
		
		Vertex schema = target.getVertex(Schema.NAMESPACE_URI);
		
		assertStatement(schema, RDF.TYPE, OWL.ONTOLOGY);
		assertStatement(schema, RDFS.COMMENT, "An ontology defined by Google, Yahoo, Bing, and others to support SEO");
		
		Vertex person = target.getVertex(Schema.Person);
		assertStatement(person, RDF.TYPE, OWL.CLASS);
		
		Vertex givenName = target.getVertex(Schema.givenName);
		assertStatement(givenName, RDF.TYPE, OWL.DATATYPEPROPERTY);
		assertStatement(givenName, RDFS.COMMENT, "The given name of the Person. In the U.S., also known as the first name.");
		assertStatement(givenName, RDFS.DOMAIN, Schema.Person);
		assertStatement(givenName, RDFS.RANGE, XMLSchema.STRING);
		
		Vertex familyName = target.getVertex(Schema.familyName);
		assertStatement(familyName, RDF.TYPE, OWL.DATATYPEPROPERTY);
		assertStatement(familyName, RDFS.COMMENT, "The family name of the Person. In the U.S., also known as the last name.");
		assertStatement(familyName, RDFS.DOMAIN, Schema.Person);
		assertStatement(familyName, RDFS.RANGE, XMLSchema.STRING);
		
	}
	@Test
	public void testRestriction() throws Exception {
		
		File oldDir = new File("src/test/resources/showl/v3");
		File newDir = new File("src/test/resources/showl/v4");
		File mergeDir = new File("target/showl/test2");
		mergeDir.mkdirs();
		
		PrintWriter reportStream = new PrintWriter(System.out);
		ModelMergeJob job = new ModelMergeJob(oldDir, newDir, mergeDir, reportStream);
		
		job.run();
		
		File targetFile = new File(mergeDir, "schema.ttl");
		assertTrue(targetFile.exists());
		
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		Graph target = new MemoryGraph();
		RdfUtil.loadTurtle(mergeDir, target, nsManager);
		
		Vertex schema = target.getVertex(Schema.NAMESPACE_URI);
		
		assertStatement(schema, RDF.TYPE, OWL.ONTOLOGY);
		
		Vertex person = target.getVertex(Schema.Person);
		assertStatement(person, RDF.TYPE, OWL.CLASS);
		
	
		
	}
	
	private void assertStatement(Vertex subject, URI predicate, Value object) {
		assertTrue(subject.hasProperty(predicate, object));
	}

	private void assertStatement(Vertex subject, URI predicate, String object) {
		Literal value = factory.createLiteral(object);
		assertTrue(subject.hasProperty(predicate, value));
	}

}

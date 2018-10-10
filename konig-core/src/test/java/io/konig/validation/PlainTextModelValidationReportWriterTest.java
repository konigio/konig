package io.konig.validation;

/*
 * #%L
 * Konig Core
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


import static org.junit.Assert.assertTrue;

import java.io.StringWriter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.Schema;
import io.konig.shacl.NodeKind;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class PlainTextModelValidationReportWriterTest {
	private ShapeManager shapeManager = new MemoryShapeManager();
	private ModelValidator validator = new ModelValidator();
	private MemoryGraph graph = new MemoryGraph(MemoryNamespaceManager.getDefaultInstance());
	private ModelValidationRequest request;
	
	@Before
	public void setUp() throws Exception {
		OwlReasoner owl = new OwlReasoner(graph);
		request = new ModelValidationRequest(owl, shapeManager);
		request.getCaseStyle().setClasses(CaseStyle.PascalCase);
		request.getCaseStyle().setProperties(CaseStyle.camelCase);
		request.getCaseStyle().setNamedIndividuals(CaseStyle.PascalCase);
		request.getCaseStyle().setNodeShapes(CaseStyle.PascalCase);
	
	}


	@Ignore
	public void test() throws Exception {
		URI fooClass = uri("http://example.com/fooClass");
		URI fooProperty = uri("http://example.com/foo_property");
		URI fooIndividual = uri("http://exmaple.com/FOO_INDIVIDUAL");
		URI personShape = uri("http://example.com/shapes/Person_Shape");
		
		edge(fooClass, RDF.TYPE, OWL.CLASS);
		edge(fooClass, RDFS.COMMENT, literal("A dummy class"));
		edge(fooProperty, RDF.TYPE, RDF.PROPERTY);
		edge(fooIndividual, RDF.TYPE, Schema.Enumeration);
		edge(fooIndividual, RDFS.COMMENT, literal("A dummy individual"));
		edge(Schema.Product, RDFS.COMMENT, literal("A product for sale"));
		
		new ShapeBuilder(shapeManager)
			.beginShape(personShape)
				.beginProperty(Schema.givenName)
					.datatype(XMLSchema.STRING)
					.valueClass(Schema.Product)
					.nodeKind(NodeKind.IRI)
				.endProperty()
				.beginProperty(Schema.familyName)
					.datatype(XMLSchema.STRING)
					.nodeKind(NodeKind.IRI)
					.beginValueShape("http://example.com/shape/ProductShape")
					.endValueShape()
				.endProperty()
				.beginProperty(Schema.parent)
					.valueClass(Schema.Person)
				.endProperty()
			.endShape();
		
		ModelValidationReport report = validator.process(request);
		
		StringWriter out = new StringWriter();
		PlainTextModelValidationReportWriter reportWriter = new PlainTextModelValidationReportWriter();
		reportWriter.writeReport(report, out);
		
		String text = out.toString();
		System.out.println(text);
		
		assertTrue(text != null);
		// TODO: Add other assertions
	}
	

	
	private Value literal(String string) {
		return new LiteralImpl(string);
	}


	private URI uri(String value) {
		return new URIImpl(value);
	}

	private void edge(URI subject, URI predicate, Value object) {
		graph.edge(subject, predicate, object);
	}

}

package io.konig.schemagen.java;

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


import java.io.File;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import com.sun.codemodel.JCodeModel;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.util.BasicJavaDatatypeMapper;
import io.konig.core.util.JavaDatatypeMapper;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.shacl.LogicalShapeBuilder;
import io.konig.shacl.LogicalShapeNamer;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.BasicLogicalShapeNamer;
import io.konig.shacl.impl.MemoryClassManager;
import io.konig.shacl.impl.SimpleShapeMediaTypeNamer;

public class AvonWriterBuilderTest {


	@Test
	public void test() throws Exception {
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		
		URI personShapeId = uri("http://example.com/shapes/v1/schema/Person");
		
		shapeBuilder
			.beginShape(personShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Konig.id)
					.nodeKind(NodeKind.IRI)
					.maxCount(1)
					.minCount(1)
				.endProperty()
				.beginProperty(Schema.givenName)
					.datatype(XMLSchema.STRING)
					.maxCount(1)
					.minCount(0)
				.endProperty()
				.beginProperty(Schema.alumniOf)
					.valueClass(Schema.Organization)
					.nodeKind(NodeKind.IRI)
				.endProperty()
				.beginProperty(Schema.email)
					.datatype(XMLSchema.STRING)
				.endProperty()
				.beginProperty(Schema.contactPoint)
					.valueShape(uri("http://example.com/shapes/v1/schema/ContactPoint"))
					.nodeKind(NodeKind.IRI)
					.maxCount(1)
					.minCount(1)
				.endProperty()
				.beginProperty(Schema.memberOf)
					.valueShape(uri("http://example.com/shapes/v1/schema/Organization"))
					.maxCount(2)
				.endProperty()
			.endShape()
			.beginShape("http://example.com/shapes/v1/schema/Organization")
				.targetClass(Schema.Organization)
				.beginProperty(Schema.name)
				.datatype(XMLSchema.STRING)
				.maxCount(1)
				.minCount(1)
				.endProperty()
			.endShape()
			.beginShape("http://example.com/shapes/v1/schema/ContactPoint")
				.targetClass(Schema.ContactPoint)
				.beginProperty(Schema.address)
					.datatype(XMLSchema.STRING)
					.maxCount(1)
					.minCount(0)
				.endProperty()
			.endShape()
			.beginShape("http://example.com/shapes/v1/schema/CreativeWork")
				.targetClass(Schema.CreativeWork)
				.beginProperty(Schema.author)
					.valueClass(Schema.Person)
					.nodeKind(NodeKind.IRI)
				.endProperty()
			.endShape()
			.beginShape("http://example.com/shapes/v1/schema/WebPage")
				.targetClass(Schema.WebPage)
				.beginProperty(Schema.lastReviewed)
					.datatype(XMLSchema.DATE)
					.maxCount(1)
					.minCount(0)
				.endProperty()
			.endShape()
		;
		
		JavaDatatypeMapper datatypeMapper = new BasicJavaDatatypeMapper();
		ShapeManager shapeManager = shapeBuilder.getShapeManager();
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		MemoryClassManager classManager = new MemoryClassManager();
		LogicalShapeNamer namer = new BasicLogicalShapeNamer("http://example.com/shapes/logical/", nsManager);

		Graph graph = new MemoryGraph();
		OwlReasoner reasoner = new OwlReasoner(graph);
		LogicalShapeBuilder builder = new LogicalShapeBuilder(reasoner, namer);
		
		builder.buildLogicalShapes(shapeManager, classManager);
		
		graph.edge(Schema.WebPage, RDFS.SUBCLASSOF, Schema.CreativeWork);
		
		JCodeModel model = new JCodeModel();
		JavaNamer javaNamer = new BasicJavaNamer("com.example.", nsManager);
		
		SimpleShapeMediaTypeNamer mediaTypeNamer = new SimpleShapeMediaTypeNamer();
		DataWriterBuilder writerBuilder = new DataWriterBuilder(graph, datatypeMapper, mediaTypeNamer, javaNamer, model);
		AvonWriterBuilder avonBuilder = new AvonWriterBuilder(writerBuilder);
		
		Shape personShape = shapeManager.getShapeById(personShapeId);
		
		avonBuilder.buildWriter(personShape);
		
		File file = new File("target/java/src");
		file.mkdirs();
		model.build(file);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}

}

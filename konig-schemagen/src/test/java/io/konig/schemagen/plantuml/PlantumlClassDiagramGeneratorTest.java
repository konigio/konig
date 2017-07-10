package io.konig.schemagen.plantuml;

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


import java.io.StringWriter;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.vocab.Schema;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.LogicalShapeBuilder;
import io.konig.shacl.LogicalShapeNamer;
import io.konig.shacl.NodeKind;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.BasicLogicalShapeNamer;
import io.konig.shacl.impl.MemoryClassManager;

public class PlantumlClassDiagramGeneratorTest {

	@Test
	public void test() throws Exception {
		
		MemoryNamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		nsManager.add("owl", OWL.NAMESPACE);
		MemoryGraph graph = new MemoryGraph(nsManager);
		

		URI shapeId = uri("http://example.com/shape/Person");
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		shapeBuilder.beginShape(shapeId)
			.targetClass(Schema.Person)
			.beginProperty(Schema.worksFor)
				.nodeKind(NodeKind.IRI)
				.valueClass(Schema.Organization)
			.endProperty()
		.endShape();

		ShapeManager shapeManager = shapeBuilder.getShapeManager();
		
		
		LogicalShapeNamer namer = new BasicLogicalShapeNamer("http://example.com/shapes/logical/", nsManager);
		OwlReasoner reasoner = new OwlReasoner(graph);
		LogicalShapeBuilder builder = new LogicalShapeBuilder(reasoner, namer);
		
		MemoryClassManager classManager = new MemoryClassManager();
		builder.buildLogicalShapes(shapeManager, classManager);
				

		SimpleValueFormat iriTemplate = new SimpleValueFormat("http://example.com/shapes/canonical/{targetClassNamespacePrefix}/{targetClassLocalName}");
		ClassStructure structure= new ClassStructure(iriTemplate, shapeManager, reasoner);
		
		PlantumlClassDiagramGenerator generator = new PlantumlClassDiagramGenerator(reasoner);
		
		StringWriter writer = new StringWriter();
		
		generator.generateDomainModel(structure, writer);
		
		String text = writer.toString();
//		assertTrue(text.contains("Person -- Organization : worksFor >"));
//		System.out.println(text);
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

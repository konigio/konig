package io.konig.schemagen.plantuml;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
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
		
		
		MemoryGraph graph = new MemoryGraph();

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
		
		MemoryNamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", Schema.NAMESPACE);
		
		LogicalShapeNamer namer = new BasicLogicalShapeNamer("http://example.com/shapes/logical/", nsManager);
		OwlReasoner reasoner = new OwlReasoner(graph);
		LogicalShapeBuilder builder = new LogicalShapeBuilder(reasoner, namer);
		
		MemoryClassManager classManager = new MemoryClassManager();
		builder.buildLogicalShapes(shapeManager, classManager);
				
		
		PlantumlClassDiagramGenerator generator = new PlantumlClassDiagramGenerator(reasoner, shapeManager);
		
		StringWriter writer = new StringWriter();
		
		generator.generateDomainModel(classManager, writer);
		
		String text = writer.toString();
		assertTrue(text.contains("Person -- Organization : worksFor >"));
//		System.out.println(text);
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}

}

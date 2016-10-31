package io.konig.schemagen.java;

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
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.shacl.LogicalShapeBuilder;
import io.konig.shacl.LogicalShapeNamer;
import io.konig.shacl.NodeKind;
import io.konig.shacl.ShapeBuilder;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.BasicLogicalShapeNamer;
import io.konig.shacl.impl.MemoryClassManager;

public class JavaClassBuilderTest {

	@Test
	public void test() throws Exception {
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		
		URI personShapeId = uri("http://example.com/shapes/v1/schema/Person");
		
		shapeBuilder
			.beginShape(personShapeId)
				.scopeClass(Schema.Person)
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
				.beginProperty(Schema.email)
					.datatype(XMLSchema.STRING)
				.endProperty()
				.beginProperty(Schema.memberOf)
					.valueClass(Schema.Organization)
					.nodeKind(NodeKind.IRI)
				.endProperty()
				.beginProperty(Schema.contactPoint)
					.valueClass(Schema.ContactPoint)
					.nodeKind(NodeKind.IRI)
					.maxCount(1)
					.minCount(0)
				.endProperty()
			.endShape()
			.beginShape("http://example.com/shapes/v1/schema/CreativeWork")
				.scopeClass(Schema.CreativeWork)
				.beginProperty(Schema.author)
					.valueClass(Schema.Person)
					.nodeKind(NodeKind.IRI)
				.endProperty()
			.endShape()
			.beginShape("http://example.com/shapes/v1/schema/WebPage")
				.scopeClass(Schema.WebPage)
				.beginProperty(Schema.lastReviewed)
					.datatype(XMLSchema.DATE)
					.maxCount(1)
					.minCount(0)
				.endProperty()
			.endShape()
		;
		
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
		JavaClassBuilder classBuilder = new JavaClassBuilder(classManager, namer, javaNamer, new OwlReasoner(graph));
		
		classBuilder.buildAll(classManager.list(), model);
		
		File file = new File("target/java/src");
		file.mkdirs();
		model.build(file);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}
}

package io.konig.schemagen.java;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;

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
import io.konig.shacl.impl.MemoryShapeManager;

public class JavaClassBuilderTest {
	
	
	@Test
	public void testOrConstraint() throws Exception {

		
		URI partyShapeId = uri("http://example.com/shapes/v1/schema/PartyShape");
		URI personShapeId = uri("http://example.com/shapes/v1/schema/PersonShape");
		URI orgShapeId = uri("http://example.com/shapes/v1/schema/OrganizationShape");
		
		URI personShapeId2 = uri("http://example.com/shapes/v2/schema/PersonShape");
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		
		shapeBuilder
		
			.beginShape(partyShapeId)
				.or(personShapeId, orgShapeId)
			.endShape()
			
			.beginShape(personShapeId)
				.targetClass(Schema.Person)
				.beginProperty(Schema.familyName)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
			.endShape()
			
			.beginShape(orgShapeId)
				.targetClass(Schema.Organization)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)				
				.endProperty()
			.endShape()
			
			
			.beginShape(personShapeId2)
				.targetClass(Schema.Person)
				.beginProperty(Schema.sponsor)
					.valueShape(partyShapeId)
					.maxCount(1)
					.minCount(0)
				.endProperty()
			.endShape()
			
			;
		
		JCodeModel model = new JCodeModel();
		NamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		ShapeManager shapeManager = shapeBuilder.getShapeManager();
		OwlReasoner owlReasoner = new OwlReasoner(new MemoryGraph());
		
		JavaNamer javaNamer = new BasicJavaNamer("com.example", nsManager);
		JavaClassBuilder classBuilder = new JavaClassBuilder(shapeManager, javaNamer, owlReasoner);
		
		classBuilder.buildClass(Schema.Person, model);

		File file = new File("target/test/JavaClassBuilderTest/orConstraint");
		file.mkdirs();
		model.build(file);
	}
	
	@Ignore
	public void testValueShape() throws Exception {
		
		URI personShapeId = uri("http://example.com/shapes/v1/schema/PersonShape");
		URI addressShapeId = uri("http://example.com/shapes/v1/schema/AddressShape");
		
		
		
		ShapeBuilder shapeBuilder = new ShapeBuilder();
		
		shapeBuilder
		
			.beginShape(personShapeId)
			
				.targetClass(Schema.Person)
				.beginProperty(Schema.name)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				
				.beginProperty(Schema.address)
					.valueShape(addressShapeId)
					.minCount(1)
					.maxCount(1)
				.endProperty()
					
				
			.endShape()
		
			.beginShape(addressShapeId)
			
				.targetClass(Schema.PostalAddress)
				.beginProperty(Schema.streetAddress)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				
				.beginProperty(Schema.addressLocality)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()

				.beginProperty(Schema.addressRegion)
					.datatype(XMLSchema.STRING)
					.minCount(1)
					.maxCount(1)
				.endProperty()
				
			.endShape();
		
		JCodeModel model = new JCodeModel();
		NamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		ShapeManager shapeManager = shapeBuilder.getShapeManager();
		OwlReasoner owlReasoner = new OwlReasoner(new MemoryGraph());
		
		JavaNamer javaNamer = new BasicJavaNamer("com.example", nsManager);
		JavaClassBuilder classBuilder = new JavaClassBuilder(shapeManager, javaNamer, owlReasoner);
		
		classBuilder.buildClass(Schema.Person, model);

		File file = new File("target/test/JavaClassBuilderTest/valueShape");
		file.mkdirs();
		model.build(file);
	}
	
	@Ignore
	public void testEnumeration() throws Exception {

		MemoryGraph graph = new MemoryGraph();
		graph.edge(Schema.GenderType, RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.GenderType, RDFS.SUBCLASSOF, Schema.Enumeration);
		graph.edge(Schema.Male, RDF.TYPE, Schema.GenderType);
		graph.edge(Schema.Female, RDF.TYPE, Schema.GenderType);
		JCodeModel model = new JCodeModel();
		NamespaceManager nsManager = MemoryNamespaceManager.getDefaultInstance();
		ShapeManager shapeManager = new MemoryShapeManager();
		OwlReasoner owlReasoner = new OwlReasoner(graph);
		
		JavaNamer javaNamer = new BasicJavaNamer("com.example", nsManager);
		JavaClassBuilder classBuilder = new JavaClassBuilder(shapeManager, javaNamer, owlReasoner);
		
		classBuilder.buildClass(Schema.GenderType, model);

		File file = new File("target/test/JavaClassBuilderTest/enumeration");
		file.mkdirs();
		model.build(file);
	}
	
	@Ignore
	public void testMultipleInheritance() throws Exception {
		MemoryGraph graph = new MemoryGraph();
		graph.edge(Schema.Place, 		 RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.Organization,  RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.LocalBusiness, RDF.TYPE, OWL.CLASS);
		graph.edge(Schema.LocalBusiness, RDFS.SUBCLASSOF, Schema.Place);
		graph.edge(Schema.LocalBusiness, RDFS.SUBCLASSOF, Schema.Organization);
		
		URI placeShapeId = uri("http://example.com/shape/v1/schema/Place");
		URI orgShapeId = uri("http://example.com/shape/v1/schema/Organization");
		
		ShapeBuilder builder = new ShapeBuilder();
		builder.beginShape(placeShapeId) 
			.targetClass(Schema.Place)
			.beginProperty(Schema.name)
				.minCount(0)
				.maxCount(1)
				.datatype(XMLSchema.STRING)
			.endProperty()
			.beginProperty(Schema.containedInPlace)
				.minCount(0)
				.valueShape(placeShapeId)
			.endProperty()
		.endShape()
		.beginShape(orgShapeId)
			.targetClass(Schema.Organization)
			.beginProperty(Schema.name)
				.minCount(1)
				.maxCount(1)
				.datatype(XMLSchema.STRING)
			.endProperty()
			

			.beginProperty(Schema.legalName)
				.minCount(0)
				.maxCount(1)
				.datatype(XMLSchema.STRING)
			.endProperty()			
		
		.endShape();
		ShapeManager shapeManager = builder.getShapeManager();
		
		OwlReasoner owlReasoner = new OwlReasoner(graph);
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");

		JCodeModel model = new JCodeModel();
		JavaNamer javaNamer = new BasicJavaNamer("com.example", nsManager);
		JavaClassBuilder classBuilder = new JavaClassBuilder(shapeManager, javaNamer, owlReasoner);
		
		classBuilder.buildClass(Schema.LocalBusiness, model);
		
		JDefinedClass javaClass = model._getClass("com.example.model.schema.LocalBusiness");
		assertTrue(javaClass != null);
		
		JMethod method = javaClass.getMethod("getName", new JType[]{});
		assertTrue(method != null);
		

		File file = new File("target/test/JavaClassBuilderTest/multipleInheritance");
		file.mkdirs();
		model.build(file);
		
	}

	@Ignore
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
		JavaClassBuilder classBuilder = new JavaClassBuilder(shapeManager, javaNamer, new OwlReasoner(graph));
		
		classBuilder.buildAll(classManager.list(), model);
		
		File file = new File("target/test/JavaClassBuilderTest/general");
		file.mkdirs();
		model.build(file);
		
	}

	private URI uri(String value) {
		return new URIImpl(value);
	}
}
